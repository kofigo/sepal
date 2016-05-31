package workerinstance

import org.openforis.sepal.component.workerinstance.api.InstanceProvider
import org.openforis.sepal.component.workerinstance.api.WorkerInstance

class FakeInstanceProvider implements InstanceProvider {
    private final Map<String, WorkerInstance> launchedById = [:]
    private final Map<String, WorkerInstance> reservedById = [:]
    private final Map<String, WorkerInstance> idleById = [:]
    private final List<WorkerInstance> terminated = []

    void launchReserved(WorkerInstance instance) {
        launchedById[instance.id] = instance
        reservedById[instance.id] = instance
    }

    void launchIdle(List<WorkerInstance> instances) {
        instances.each {
            launchedById[it.id] = it
            idleById[it.id] = it
        }
    }

    void terminate(String instanceId) {
        def instance = launchedById.remove(instanceId)
        reservedById.remove(instanceId)
        idleById.remove(instanceId)
        terminated << instance
    }

    void reserve(WorkerInstance instance) {
        assert instance.reservation
        assert idleById.remove(instance.id),
                "Instance must be idle before it can be reserved"
        reservedById[instance.id] = instance
        launchedById[instance.id] = instance
    }

    void release(String instanceId) {
        def instance = reservedById.remove(instanceId)
        assert instance,
                "Instance must be reserved before it can be idle. Reserved instances: ${reservedById.values()}"
        def releasedInstance = instance.release()
        idleById[instanceId] = releasedInstance
        launchedById[instanceId] = releasedInstance
    }

    List<WorkerInstance> idleInstances(String instanceType) {
        idleInstances().findAll { it.type == instanceType }
    }

    List<WorkerInstance> idleInstances() {
        idleById.values().toList()
    }

    List<WorkerInstance> reservedInstances() {
        reservedById.values().toList()
    }

    WorkerInstance getInstance(String instanceId) {
        launchedById[instanceId]
    }

    WorkerInstance launchedOne() {
        assert allInstances().size() == 1,
                "Expected one instance to have been launched. Actually launched ${allInstances().size()}: ${allInstances()}"
        return allInstances().first()
    }

    WorkerInstance reservedOne() {
        assert reservedById.size() == 1,
                "Expected one instance to have been reserved. Actually reserved ${reservedById.size()}: ${reservedById.values()}"
        return reservedById.values().first()
    }

    void noIdle() {
        assert idleById.isEmpty(),
                "Expected no instance to be idle. Actually has ${idleById.size()} idle: ${idleById.values()}"
    }

    WorkerInstance oneIdle() {
        assert idleById.size() == 1,
                "Expected one instance to be idle. Actually has ${idleById.size()} idle: ${idleById.values()}"
        return idleById.values().first()
    }

    WorkerInstance terminatedOne() {
        assert terminated.size() == 1,
                "Expected one instance to be terminated. Actually terminated ${terminated.size()}: ${terminated}"
        return terminated.first()
    }

    private List<WorkerInstance> allInstances() {
        launchedById.values().toList()
    }
}
