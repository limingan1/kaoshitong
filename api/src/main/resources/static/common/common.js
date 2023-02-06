function commonLogout(baseApi,token){
    axios.get(baseApi + "/node/logout", {headers: {token: token,"X-Frame-Options":'DENY'}}).then(res => {
        if (res.status === 200) {
            window.location = "/";
        }
    }).catch(e => {
        let res = e.response
        if (res) {
            dealUnauthorized(res,window)
        }
    });
}

function dealUnauthorized(res,window) {
    if (res.status === 401) {
        window.location = "/";
    }
}

function keepAlive(_this,token,window,baseApi) {
    let url = baseApi + "/node/keepAlive?timestamp=" + new Date().getTime();
    axios.get(url,{headers:{token:token,"X-Frame-Options":'DENY'}}).catch(e=>{
        let res = e.response
        if(res){
            if (res.status === 401) {
                // clearInterval(id);
                let msg = res.data.message ? res.data.message : _this.i18n('SessionTimeOut', _this.i18n('ReRegister'));
                _this.$alert(msg, _this.i18n('Tip'), {
                    confirmButtonText: _this.i18n('ConfirmBtn'),
                    callback: () => {
                        window.location = "/";
                    }
                });
            }
        }
    })
}
function checkToken(_this,window,baseApi){
    let id;
    id = setInterval(function () {
        let url = baseApi + "/node/time?timestamp=" + new Date().getTime();
        axios.get(url,{headers:{token:_this.token,"X-Frame-Options":'DENY'}}).catch(e=>{
            let res = e.response
            if(res){
                if (res.status === 401) {
                    clearInterval(id);
                    _this.$alert(_this.i18n('SessionTimeOut',_this.i18n('ReRegister')), _this.i18n('Tip'), {
                        confirmButtonText: _this.i18n('ConfirmBtn'),
                        callback: () => {
                            window.location = "/";
                        }
                    });
                }
            }
        })
    }, 5000);
}
function commonQueryNodeList(url,_this,window,callback){
    axios.get(url,{headers: {token: _this.token,"X-Frame-Options":'DENY'}}).then(res=>{
        if (res.status === 200) {
            callback(res.data);
        }
    }).catch(e=>{
        let res = e.response
        dealUnauthorized(res,window)
    })
}
function dealResponse(e,_this){
    let res = e.response
    if(res){
        ElMessage({
            message: _this.i18n(e.response.data.errorDesc),
            type: 'error',
        });
        if (res.status === 401) {
            ElMessage({
                message: _this.i18n('SessionTimeOut'),
                type: 'info',
            });
            window.location = "/";
        }
    }
}
function commonTest(formName,_this,addUrl,updateUrl,notCheck){
    _this.$refs[formName].validate((valid) => {
        if (valid) {
            let formData = {..._this.form}
            if (!notCheck && formData.type !== 0 && (formData.ip === undefined || formData.ip === null || formData.ip.trim() === '')) {
                return;
            }
            if (formData.password !== undefined && formData.password === '******') {
                formData.password = undefined;
            }
            axios({
                method: 'POST',
                url: _this.formType === 'update' ? updateUrl : addUrl,
                data: formData,
                headers: {token: _this.token,"X-Frame-Options":'DENY'}
            }).then(response => {
                if (response.status === 200) {
                    ElMessage({
                        message: _this.i18n('TestSuccess'),
                        type: 'success',
                    })
                    _this.dealSuccessRes();
                }
            }).catch(e => {
                dealResponse(e,_this)
            });
        }else{
        }
    });
}

function commonAdd(formName,_this,url,notCheck,callback,updateCallback){
    _this.$refs[formName].validate((valid) => {
        if (valid) {
            let formData = {..._this.form}
            if (formData.type !== -1 && formData.businessType !== 1 && !notCheck) {
                formData.permissionSwitch = undefined;
            }
            if (!notCheck && formData.type !== 0 && (formData.ip === undefined || formData.ip === null || formData.ip.trim() === '')) {
                return;
            }
            if (_this.formType === 'update') {
                if(updateCallback){
                    updateCallback();
                }else{
                    _this.updateSubmit(formData);
                }
                return;
            }
            axios({
                method: 'POST',
                url: url,
                data: formData,
                headers: {token: _this.token,"X-Frame-Options":'DENY'}
            }).then(response => {
                if (response.status === 200) {
                    ElMessage({
                        message: _this.i18n('AddSuccess'),
                        type: 'success',
                    })
                    _this.dialogVisible = false
                    callback();
                    _this.form = {type: 0,businessType: 1,ssl: 1};
                }
            }).catch(e => {
                dealResponse(e,_this)
            });
        }else{
        }
    });
}

function commonUpdateSubmit(_this,param,url,callback){
    axios({
        method: 'PUT',
        url: url,
        data: param,
        headers: {token: _this.token,"X-Frame-Options":'DENY'}
    }).then(response => {
        if (response.status === 200) {
            ElMessage({
                message: _this.i18n('UpdateSuccess'),
                type: 'success',
            })
            _this.dialogVisible = false
            callback();
            _this.form = {type: 0,businessType: 1,ssl: 1};
        }
    }).catch(e => {
        dealResponse(e,_this)
    });
}

function commonDelete(_this,url,callback){
    _this.$confirm(_this.i18n('DeleteTip'), _this.i18n('Tip'), {
        confirmButtonText: _this.i18nText.ConfirmBtn,
        cancelButtonText: _this.i18nText.CancelBtn,
        type: 'warning'
    }).then(() => {
        axios.delete(url,{headers: {token: _this.token,"X-Frame-Options":'DENY'}}).then(res=>{
            if (res.status === 200) {
                _this.$message({
                    type: 'success',
                    message: _this.i18n('DeleteSuccess')
                });
                callback();
            }
        }).catch(e=>{
            let res = e.response
            if(res){
                dealResponse(e,_this)
            }
        })
    });
}

function commonQueryOrg(_this,baseApi){
    let url = baseApi + "/node/vm/organizations";
    axios.get(url,{headers: {token: _this.token,"X-Frame-Options":'DENY'}}).then(res=>{
        if (res.status === 200) {
            _this.organizations = res.data ? res.data : [];
            _this.organizations.forEach(item=>{
                item.value=item.id;
                item.label=item.name;
            })
            _this.dealSuccessRes();
        }
    }).catch(e=>{
        let res = e.response
        dealUnauthorized(res,window)
    })
}