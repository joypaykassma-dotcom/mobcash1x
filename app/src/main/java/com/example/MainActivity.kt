package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.WalletDatabase
import com.example.data.WalletRepository
import com.example.ui.WalletViewModel
import com.example.ui.WalletViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = WalletDatabase.getDatabase(this)
        val repository = WalletRepository(database.walletDao())
        
        // Initialize Realtime Firebase Bidirectional Synchronization
        com.example.data.FirebaseSyncManager.initSync(database)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WalletViewModel = viewModel(factory = WalletViewModelFactory(repository))
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                repository = repository,
                                onLoginSuccess = { userId, role ->
                                    viewModel.login(userId)
                                    val destination = if (role == "Admin") "admin_dashboard" else "home"
                                    navController.navigate(destination) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateSignup = { role -> navController.navigate("signup/$role") },
                                onNavigateForgetPassword = { navController.navigate("forget_password") }
                            )
                        }
                        composable("signup/{role}") { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "Personal"
                            SignupScreen(
                                repository = repository,
                                initialRole = role,
                                onSignupSuccess = { userId ->
                                    navController.navigate("signup_success/$userId") {
                                        popUpTo("login") { inclusive = false }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("signup_success/{userId}") { backStackEntry ->
                            val userIdStr = backStackEntry.arguments?.getString("userId")
                            val userId = userIdStr?.toIntOrNull() ?: return@composable
                            SignupSuccessScreen(
                                userId = userId,
                                viewModel = viewModel,
                                onContinue = {
                                    navController.navigate("login") {
                                        popUpTo("signup_success") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("forget_password") {
                            ForgetPasswordScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("admin_dashboard") {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onNavigateUserDetail = { userId ->
                                    navController.navigate("admin_user_detail/$userId")
                                },
                                onNavigateStatement = {
                                    navController.navigate("statement")
                                },
                                onNavigateConfig = {
                                    navController.navigate("admin_config")
                                },
                                onNavigateTickets = {
                                    navController.navigate("admin_tickets")
                                },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("admin_dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("admin_user_detail/{userId}") { backStackEntry ->
                            val userIdStr = backStackEntry.arguments?.getString("userId")
                            val userId = userIdStr?.toIntOrNull() ?: return@composable
                            AdminUserDetailScreen(
                                userId = userId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("admin_tickets") {
                            AdminTicketsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateAction = { actionType ->
                                    navController.navigate("action/$actionType")
                                },
                                onNavigateProfile = { navController.navigate("profile") },
                                onNavigateStatement = { navController.navigate("statement") },
                                onNavigateOffer = { navController.navigate("offer") },
                                onNavigateSupport = { navController.navigate("support") },
                                onNavigateReferDashboard = { navController.navigate("refer_dashboard") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("offer") {
                            OfferScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("profile") {
                            ProfileScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("statement") {
                            StatementScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("admin_config") {
                            AdminConfigScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("refer_dashboard") {
                            ReferDashboardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("support") {
                            SupportScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("action/{actionType}") { backStackEntry ->
                            val actionType = backStackEntry.arguments?.getString("actionType") ?: "Action"
                            ActionScreen(
                                actionType = actionType,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

