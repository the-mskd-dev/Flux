package com.mskd.flux

import com.mskd.flux.report.moduleReport
import org.koin.dsl.module

val moduleFlavor = module {

    includes(
        moduleReport
    )

}