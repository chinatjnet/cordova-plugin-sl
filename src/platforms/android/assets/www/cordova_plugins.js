cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/cordova-plugin-sl/sl.js",//js文件路径
        "id": "cordova-plugin-sl.sl",//js cordova.define的id
        "clobbers": [
            "sl"//js 调用时的方法名
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-whitelist": "1.3.1"
};
// BOTTOM OF METADATA
});