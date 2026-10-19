package com.mskd.flux.navigation.domain

class FluxNavigator(
    val navigate: (Route) -> Unit,
    val onBack: () -> Unit,
    val clearAndNavigate: (Route) -> Unit,
)