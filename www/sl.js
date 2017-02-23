var exec = require("cordova/exec");
module.exports = {
    start: function(){
        exec(
        function(message){//成功回调function
            console.log(message);
        },
        function(message){//失败回调function
            console.log(message);
        },
        "Screenlock",//feature name
        "start",//action
        []//要传递的参数，json格式
        );
    },
    show_screen: function(){
        exec(
        function(message){//成功回调function
            console.log(message);
        },
        function(message){//失败回调function
            console.log(message);
        },
        "Screenlock",//feature name
        "show_screen",//action
        []//要传递的参数，json格式
        );
    },
    hide_screen: function(){
        exec(
        function(message){//成功回调function
            console.log(message);
        },
        function(message){//失败回调function
            console.log(message);
        },
        "Screenlock",//feature name
        "hide_screen",//action
        []//要传递的参数，json格式
        );
    },
    show_setting: function(){
        exec(
        function(message){//成功回调function
            console.log(message);
        },
        function(message){//失败回调function
            console.log(message);
        },
        "Screenlock",//feature name
        "show_setting",//action
        []//要传递的参数，json格式
        );
    },
    hide_setting: function(){
        exec(
        function(message){//成功回调function
            console.log(message);
        },
        function(message){//失败回调function
            console.log(message);
        },
        "Screenlock",//feature name
        "hide_setting",//action
        []//要传递的参数，json格式
        );
    },
    get_screen: function(successFunc, failFunc){
        exec(
        successFunc,
        failFunc,
        "Screenlock",//feature name
        "get_screen",//action
        []//要传递的参数，json格式
        );
    },
    get_setting: function(successFunc, failFunc){
        exec(
        successFunc,
        failFunc,
        "Screenlock",//feature name
        "get_setting",//action
        []//要传递的参数，json格式
        );
    },
    fb_banner: function(content, successFunc, failFunc) {
        exec(
        successFunc,
        failFunc,
        "Screenlock",
        "fb_banner",
        [content]
        )
    }
}