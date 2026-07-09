package com.mskd.flux.core.model.artwork

/**
 * Represents the viewing status of an media or episode.
 *
 * - TO_WATCH Indicates the item is yet to be watched.
 * - IS_WATCHING Indicates the item is currently being watched.
 * - WATCHED Indicates the item has been watched.
 */
enum class Status { TO_WATCH, IS_WATCHING, WATCHED }