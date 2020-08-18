package com.fossil.trackme.base

interface BaseView {
    /**
     * Init and set views event like onClick, onTextChanged ...
     */
    fun initViews()

    /**
     * Set data observe with ViewModel if have to
     */
    fun observeData()
}