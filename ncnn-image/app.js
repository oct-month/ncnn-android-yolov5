// const dotenv = require('dotenv')
const fs = require('fs')
const express = require('express')
const multer = require('multer')
const crypto = require('crypto')
const path = require('path')

const logger = require('./logger')
const config = require('./config')

// var denv = dotenv.config({
//     path: './.env',
//     encoding: 'utf8'
// })
// if (denv.error) {
//     throw denv.error
// }
// logger.debug('load env', denv.parsed)

// const storage = multer.diskStorage({
//     destination: (req, file, cb) => {
//         cb(null, 'public/uploads')
//     },
//     filename: (req, file, cb) => {
//         var sum = crypto.createHash('sha256')
//         var extname = path.extname(file.originalname)
        
//         file.stream
//             .on('data', (chunk) => {
//                 sum.update(chunk)
//             })
//             .on('end', () => {
//                 var sha = sum.digest('hex')
//                 cb(null, sha + extname)
//             })
//             .on('error', (err) => {
//                 logger.error(err)
//                 cb(new Error('multer: 文件读取错误'))
//             })
//     }
// })
const upload = multer({
    storage: multer.memoryStorage(),
    limits: {
        fileSize: 4 * 1024 * 1024,  // 限制4MB
        files: 1
    },
    fileFilter(req, file, cb) {
        var re = /(\.jpg|\.png|\.jpeg)$/i
        if (file.originalname.match(re)) {
            cb(null, true)
        }
        else {
            cb(new Error('仅支持jpg或png图片'), false)
        }
    }
})
const app = express()

// 静态资源
app.use(express.static('public'))


app.post('/api/image', (req, res, next) => {
    upload.single('upload')(req, res, (err) => {
        if (err) {
            res.json({
                err: err.message
            }, 400)
        }
        else {
            var sum = crypto.createHash('sha256')
            sum.update(req.file.buffer.toString('hex'))
            var sha = sum.digest('hex')
            var filename = sha + path.extname(req.file.originalname)
            var filepath = path.join('public/uploads', filename)
            logger.debug(req.body)
            if (!fs.existsSync(filepath) || req.body['cover']) {
                fs.writeFileSync(filepath, req.file.buffer)
            }
            else {
                logger.debug(`${filepath} 已存在`)
            }

            res.json({
                path: '/uploads/' + filename
            })
        }
    })
})

app.get('/api/image', (req, res, next) => {
    var re = /(\.jpg|\.png|\.jpeg)$/i
    var imgList = fs.readdirSync('./public/uploads').filter((val) => {
        if (val.match(re)) {
            return true
        }
        else {
            return false
        }
    })
    res.json({
        imglist: imgList
    })
})


app.listen(config.serverPort, () => {
    logger.debug(`Server start on http://127.0.0.1:${config.serverPort}`)
})
