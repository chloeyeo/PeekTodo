package com.chloeyeo.peektodo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chloeyeo.peektodo.ui.settings.SettingsRoute
import com.chloeyeo.peektodo.ui.todo.TodoListRoute

object Routes {
    const val TODOS = "todos"
    const val SETTINGS = "settings"
}

@Composable
fun PeekTodoNavHost(revealKey: Int) {
    val navController = rememberNavController()

    // A reveal request (notification tap in blur mode) must land on the list,
    // even if the app was left on another screen.
    LaunchedEffect(revealKey) {
        if (revealKey != 0) navController.popBackStack(Routes.TODOS, inclusive = false)
    }

    NavHost(navController = navController, startDestination = Routes.TODOS) {
        composable(Routes.TODOS) {
            TodoListRoute(
                revealKey = revealKey,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
