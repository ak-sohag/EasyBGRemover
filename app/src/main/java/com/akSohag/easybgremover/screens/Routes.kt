package com.akSohag.easybgremover.screens

import kotlinx.serialization.Serializable

/**
 * Created by ak-sohag on 3/6/2025.
 */


@Serializable
data object SplashRoute

@Serializable
data object HomeRoute

@Serializable
data class EditorRoute(val uriString: String)