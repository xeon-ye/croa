function hasRoleCW() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWCN() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='CN'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWZZ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='ZZ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWZJ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='ZJ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWBZ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='BZ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleMJBZ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='MJ'&&user.roles[i].code=='BZ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleMJZZ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='MJ'&&user.roles[i].code=='ZZ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleMJZJ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='MJ'&&user.roles[i].code=='ZJ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWKJ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='KJ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWYG() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='YG'){
            return true ;
        }
    }
    return false ;
}
function hasRoleCWZL() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='CW'&&user.roles[i].code=='ZL'){
            return true ;
        }
    }
    return false ;
}

function hasRoleXM() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='XM'){
            return true ;
        }
    }
    return false ;
}

function hasRoleYW() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='YW'){
            return true ;
        }
    }
    return false ;
}
function hasRoleRS() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='RS'){
            return true ;
        }
    }
    return false ;
}
function hasRoleXZ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='XZ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleMJ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='MJ'){
            return true ;
        }
    }
    return false ;
}
function hasRoleJT() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='JT'){
            return true ;
        }
    }
    return false ;
}
function hasRoleZJB() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='ZJB'){
            return true ;
        }
    }
    return false ;
}
function hasRoleYWYG() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='YW'&&user.roles[i].code=='YG'){
            return true ;
        }
    }
    return false ;
}
function hasRoleYWZL() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='YW'&&user.roles[i].code=='ZL'){
            return true ;
        }
    }
    return false ;
}
function hasRoleXT() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='XT'){
            return true ;
        }
    }
    return false ;
}
// 客户管理员
function hasRoleKHGLY() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if(user.roles[i].type=='KH' && user.roles[i].code=='GLY' ){
            return true ;
        }
    }
    return false ;
}
function resize(table) {
    if (table == undefined) return;
    var width = $(table).parents(".jqGrid_wrapper").width();
    if (width == 0) return;
    $(table).setGridWidth(width);
}