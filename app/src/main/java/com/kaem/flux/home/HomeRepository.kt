package com.kaem.flux.home

import com.kaem.flux.model.DataState
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor() {

    fun getLocalFiles() = flow<DataState<String>> {  }
}