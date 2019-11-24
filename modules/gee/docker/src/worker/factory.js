const {Subject} = require('rxjs')
const {finalize, first, map, filter} = require('rxjs/operators')
const {Worker, MessageChannel} = require('worker_threads')
const {deserializeError} = require('serialize-error')
const {v4: uuid} = require('uuid')
const path = require('path')
const _ = require('lodash')
const log = require('@sepal/log')

const WORKER_PATH = path.join(__dirname, 'worker.js')

const createWorker = () =>
    new Worker(WORKER_PATH)

const createChannels = names =>
    _.transform(names, (data, name) => {
        const {port1: localPort, port2: remotePort} = new MessageChannel()
        data.localPorts[name] = localPort
        data.remotePorts[name] = remotePort
    }, {
        localPorts: {},
        remotePorts: {}
    })

const bootstrapWorker$ = (name, channelNames) => {
    const worker$ = new Subject()
    const worker = createWorker()
    const {localPorts, remotePorts} = createChannels(channelNames)
    worker.once('message', status => {
        if (status === 'READY') {
            worker$.next({worker, ports: localPorts})
        } else {
            worker$.error('Cannot initialize worker.')
        }
    })
    worker.postMessage({name, ports: remotePorts}, _.values(remotePorts))
    return worker$.pipe(
        first()
    )
}

const initWorker$ = (name, jobPath) => {
    const msg = (msg, jobId) => [
        `[${name}${jobId ? `.${jobId.substr(-4)}` : ''}]`,
        msg
    ].join(' ')
    
    const result$ = new Subject()

    const handleWorkerMessage = message => {
        message.value && handleValue(message)
        message.error && handleError(message)
        message.complete && handleComplete(message)
    }

    const handleValue = ({jobId, value}) => {
        log.trace(msg(`value: ${value}`, jobId))
        result$.next({jobId, value})
    }

    const handleError = ({jobId, error: serializedError}) => {
        const error = deserializeError(serializedError)
        const errors = _.compact([
            error.message,
            error.type ? `(${error.type})` : null
        ]).join()
        log.error(msg(`error: ${errors}`, jobId))
        result$.next({jobId, error})
    }

    const handleComplete = ({jobId, complete}) => {
        log.debug(msg('completed', jobId))
        result$.next({jobId, complete})
    }

    const submit$ = port => {
        const send = msg => port.postMessage(msg)

        port.on('message', handleWorkerMessage)

        return args => {
            const jobId = uuid()
            const jobResult$ = new Subject()

            result$.pipe(
                filter(message => message.jobId === jobId)
            ).subscribe(
                message => {
                    message.value && jobResult$.next({value: message.value})
                    message.error && jobResult$.error({error: message.error})
                    message.complete && jobResult$.complete()
                },
                error => log.error(error), // how to handle this?
                complete => log.warn(complete) // how to handle this?
            )

            const start = jobId => {
                const workerArgs = _.last(args)
                _.isEmpty(workerArgs)
                    ? log.debug(msg('started with no args', jobId))
                    : log.debug(msg('started with args:', jobId), workerArgs)
                send({jobId, start: {jobPath, args}})
            }

            const stop = jobId =>
                send({jobId, stop: true})

            start(jobId)
            return jobResult$.pipe(
                finalize(() => stop(jobId))
            )
        }
    }

    const dispose = (worker, port) => {
        port.off('message', handleWorkerMessage)
        worker.unref() // is this correct? terminate() probably isn't...
        log.info(msg('disposed'))
    }

    const dispose$ = (worker, jobPort) => {
        const dispose$ = new Subject()
        dispose$.pipe(
            first()
        ).subscribe(
            () => dispose(worker, jobPort)
        )
        return dispose$
    }

    return bootstrapWorker$(name, ['job']).pipe(
        map(({worker, ports: {job: jobPort}}) => {
            log.trace('Worker ready')
            return {
                submit$: submit$(jobPort),
                dispose$: dispose$(worker, jobPort)
            }
        }))
}

module.exports = {
    initWorker$
}
