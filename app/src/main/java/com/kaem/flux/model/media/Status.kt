package com.kaem.flux.model.media

import com.kaem.flux.model.media.Status.IS_WATCHING
import com.kaem.flux.model.media.Status.TO_WATCH
import com.kaem.flux.model.media.Status.WATCHED


/**
 * Represents the viewing status of an media or episode.
 *
 * @property TO_WATCH Indicates the item is yet to be watched.
 * @property IS_WATCHING Indicates the item is currently being watched.
 * @property WATCHED Indicates the item has been watched.
 */
enum class Status { TO_WATCH, IS_WATCHING, WATCHED }