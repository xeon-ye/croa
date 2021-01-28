//目标
var Goal = {
    //目标年度
    year: 2018,
    //目标用户
    userId: 1,
    departId: 1,
    companyId: 1,

    init:function(){
        $("input").keyup(
            function(){
                var jdName = "j"+Goal.getJd(this.name)+"t"+Goal.getType(this.name);
                var yName = "y1t"+Goal.getType(this.name);
                var currentVal = Goal.getVal(this.value) || 0;
                var jdVal = Goal.getVal($("[name="+jdName+"]").val()) || 0;
                var yVal = Goal.getVal($("[name="+yName+"]").val()) || 0;
                $("[name="+jdName+"]").val(jdVal+currentVal-Goal.getVal(this.pre));
                $("[name="+yName+"]").val(yVal+currentVal-Goal.getVal(this.pre));
                this.pre = currentVal;
            }
        );
    },
    getVal:function(val){
        var reg = /^[0-9]+.?[0-9]*$/;
        if(reg.test(val)){
            return parseFloat(val);
        }else{
            return "";
        }
    },
    //得到季度
    getJd:function(val){
        var reg = /j(\S*)m/;
        return val.match(reg)[1];
    },
    //得到type
    getType:function(val){
        var reg = /t(\S*)/;
        return val.match(reg)[1];
    },
    changeValue:function(obj){

    }
};