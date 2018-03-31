import React from 'react'
import {connect} from 'react-redux'
import {AnimateEnter, AnimateReplacement} from '../../widget/animate'
import CenteredPanel from 'widget/centered-panel'
import Icon from 'widget/icon'
import SlideShow from './slideshow/slideshow'
import {Msg} from 'translate'
import Login from './login'
import ForgotPassword from './forgot-password'
import ResetPassword from './reset-password'
import SetupAccount from './setup-account'
import styles from './landing.module.css'
import {location, Route, Switch} from 'route'
import PropTypes from 'prop-types'

const mapStateToProps = () => ({
    location: location()
})

let Landing =
    ({location}) =>
        <div className={styles.landing}>
            <SlideShow/>
            <LandingPanel>
                <AnimateEnter name={AnimateEnter.fadeInUp} delay={1000}>
                    <Caption/>
                </AnimateEnter>

                <AnimateEnter name={AnimateEnter.fadeInUp} delay={0}>
                    <Title/>
                </AnimateEnter>

                <AnimateEnter name={AnimateEnter.fadeInLeft} delay={100}>
                    <Features/>
                </AnimateEnter>

                <AnimateEnter name={AnimateEnter.fadeInRight} delay={1500} className={styles.form}>
                    <AnimateReplacement
                        currentKey={location.pathname}
                        classNames={{enter: styles.formEnter, exit: styles.formExit}}
                        style={{height: '100%'}}>
                        <Form location={location}/>
                    </AnimateReplacement>
                </AnimateEnter>
            </LandingPanel>
        </div>

const LandingPanel = ({children}) =>
    <CenteredPanel className={styles.landingPanel}>
        <div className={styles.contentContainer}>
            {children}
        </div>
    </CenteredPanel>

LandingPanel.propTypes = {
    children: PropTypes.any
}

const Caption = () =>
    <p className={styles.caption}>
        <Msg id='landing.caption'/>
    </p>

const Title = () =>
    <div className={styles.titleSection}>
        <h2 className={styles.title}><Msg id='landing.title'/></h2>
        <hr className={styles.titleUnderline}/>
    </div>

const Features = () =>
    <div className={styles.features}>
        <Feature name='process' icon='globe'/>
        <Feature name='browse' icon='folder-open'/>
        <Feature name='apps' icon='wrench'/>
        <Feature name='terminal' icon='terminal'/>
    </div>

const Feature = ({icon, name}) =>
    <div className={styles.feature}>
        <Icon
            name={icon}
            className={`${styles.featureIcon} ${styles[`${name}Icon`]}`}
        />
        <h3 className={styles.featureTitle}><Msg id={`landing.features.${name}.title`}/></h3>
        <p className={styles.featureDescription}><Msg id={`landing.features.${name}.description`}/></p>
    </div>

Feature.propTypes = {
    icon: PropTypes.string,
    name: PropTypes.string
}

const Form = ({location}) =>
    <Switch location={location}>
        <Route path='/forgot-password' component={ForgotPassword}/>
        <Route path='/reset-password' component={ResetPassword}/>
        <Route path='/setup-account' component={SetupAccount}/>
        <Route path='/' component={Login}/>
    </Switch>

Form.propTypes = {
    location: PropTypes.object
}

export default connect(mapStateToProps)(Landing)
