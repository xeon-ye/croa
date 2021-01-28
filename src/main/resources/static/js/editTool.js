/*
调用方式:
    引包:
    <link rel="stylesheet" href="/js/plugins/kindeditor/themes/default/default.css" />
    <script charset="utf-8" src="/js/plugins/kindeditor/kindeditor-all-min.js"></script>
    <script charset="utf-8" src="/js/plugins/kindeditor/lang/zh-CN.js"></script>
    <script type="text/javascript" src="/js/editTool.js"></script>

1、创建元素：
   指定元素id例如  <textarea id="editTool" style="width:800px;height:400px;"></textarea>
2、创建对象：
   var editToolObj = new KindeditorTool({
                targetEl: "#editTool",
                uploadUrl: "/editUpload?filePart=kindEditor"
   });
3、参数说明：
   targetEl - textarea元素ID名称，类型：String，例如：#textareaID
   uploadUrl - 文件上传接口地址，类型：String，文件可为为image、flash、media、file
   fileManagerUrl - 图片空间接口地址，类型：String
   filePart - 指定文件上传目录名，类型：String，一般目录名称根据当前功能类型来命名
   items - 工具栏，其中”/”表示换行，”|”表示分隔符，默认所有功能，类型：Array
   filterItems - 工具栏中不展示的功能，类型Array
4、方法说明：
   getContent - 获取富文本编辑器内容
   setContent - 设置富文本编辑器内容
*/
function KindeditorTool(config) {
    this.targetEl = config.targetEl;
    this.uploadUrl = config.uploadUrl || '/editUpload?filePart='+(config.filePart || 'kindEditor');
    this.fileManagerUrl = config.fileManagerUrl || '';
    this.allowFileManager = config.fileManagerUrl ? true : false;
    this.filterItems = config.filterItems || [];
    this.items = config.items || [
        'source', '|', 'undo', 'redo', '|', 'preview', 'print', 'template', 'code', 'cut', 'copy', 'paste',
        'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
        'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
        'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
        'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
        'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage',
        'flash', 'media', 'insertfile', 'table', 'hr', 'emoticons', 'baidumap', 'pagebreak',
        'anchor', 'link', 'unlink', '|', 'about'
    ];
    this.init();
}

KindeditorTool.prototype = {
    constructor: KindeditorTool,
    _this: this,
    editorObj: null,
    init: function () {
        var _this = this;
        //如果有过滤item则，进行过滤
        if(_this.filterItems && _this.filterItems.length > 0){
            for(var i = 0; i < _this.items.length; i++){
                for(var j = 0; j < _this.filterItems.length; j++){
                    if(_this.items[i] == _this.filterItems[j]){
                        _this.items.splice(i, 1); //删除指定元素
                    }
                }
            }
        }
        KindEditor.ready(function(K) {
            _this.editorObj =  K.create(_this.targetEl, {
                items: _this.items,
                uploadJson : _this.uploadUrl,
                fileManagerJson : _this.fileManagerUrl,
                allowFileManager : _this.allowFileManager,
            });
        });
    },
    getContent: function () {
        var _this = this;
        if(_this.editorObj){
            _this.editorObj.sync(); //同步数据后可以直接取得textarea的value
        }
        return $(_this.targetEl).val();
    },
    setContent: function (content) {
        var _this = this;
        if(_this.editorObj){
            _this.editorObj.html(content);
        }else {
            $(_this.targetEl).html(content);
        }
    },
    readonly: function (isReadOnly) {
        var _this = this;
        _this.editorObj.readonly(isReadOnly); //false时取消只读状态，true时设置成只读状态
    }
}