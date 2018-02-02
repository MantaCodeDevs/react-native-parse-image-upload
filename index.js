const { NativeModules } = require('react-native')
const { ParseImageUpload } = NativeModules

export default {
    uploadImageAsJpg: (imagePath, fileName = null, newWidth = 0, newHeight = 0, rotation = -1) => {
        return ParseImageUpload.uploadImage(imagePath, fileName, newWidth, newHeight, rotation)
    },
}
