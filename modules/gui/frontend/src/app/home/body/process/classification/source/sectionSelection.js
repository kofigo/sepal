import {msg} from 'translate'
import Buttons from 'widget/buttons'
import PropTypes from 'prop-types'
import React from 'react'
import styles from './sectionSelection.module.css'

export default class SectionSelection extends React.Component {
    render() {
        const {section} = this.props
        const options = [
            {
                value: 'RECIPE_REF',
                label: msg('process.classification.panel.source.recipe.title')
            },
            {
                value: 'ASSET',
                label: msg('process.classification.panel.source.asset.title')
            },
        ]
        return (
            <Buttons
                type='vertical'
                className={styles.sectionSelection}
                input={section}
                options={options}/>
        )
    }
}

SectionSelection.propTypes = {
    section: PropTypes.object.isRequired
}