package com.rainguard.ai.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN_ROLE = "login_role"
    const val HOME_MAP = "home_map"
    const val EVACUATION_ROUTE = "evacuation_route/{routeId}"
    const val ALERTS = "alerts"
    const val REPORT = "report"
    const val CHAT = "chat"
    const val SHELTER = "shelter/{shelterId}"
    const val AUTHORITY_DASHBOARD = "authority_dashboard"
    const val ALL_REPORTS = "all_reports"
    const val SETTINGS = "settings"

    fun evacuationRoute(routeId: String) = "evacuation_route/$routeId"
    fun shelter(shelterId: String) = "shelter/$shelterId"
}