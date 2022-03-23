const log4js = require('log4js')

const config = require('./config')

log4js.configure({
    appenders: {
        monitor: {
            type: 'file',
            filename: config.logfile
        },
        console: {
            type: 'console'
        }
    },
    categories: {
        default: {
            appenders: ['monitor', 'console'],
            level: 'debug'
        }
    }
})

module.exports = log4js.getLogger('monitor')
