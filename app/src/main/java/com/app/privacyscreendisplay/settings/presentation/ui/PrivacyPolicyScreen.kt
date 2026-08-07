package com.app.privacyscreendisplay.settings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Comprehensive Google Play Store Compliant Privacy Policy Screen.
 */
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Privacy Policy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Highlight Badge Card: 100% On-Device Processing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "100% On-Device AI Processing",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your camera stream is processed locally in real-time. No video, images, or face biometric data are ever recorded or transmitted.",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PolicySection(
                title = "1. Information We Process",
                content = "Shoulder Surfing Guard AI uses your device front camera solely to perform real-time optical detection of additional faces looking over your shoulder ('shoulder surfing'). Camera frames are processed strictly in RAM using on-device machine learning models and are immediately discarded. We do not store, record, upload, or share any imagery or video frames."
            )

            PolicySection(
                title = "2. Permissions Usage",
                content = "Our application requests the following Android permissions strictly to deliver its core security features:\n" +
                        "• Camera Access: Used strictly for real-time local shoulder surfing detection.\n" +
                        "• System Overlay (Draw Over Apps): Used to render the protective blur overlay when a shoulder surfer is detected.\n" +
                        "• Usage Access: Used to identify when configured Protected Apps are launched in the foreground."
            )

            PolicySection(
                title = "3. Advertising & Analytics",
                content = "We use Google AdMob to display advertisements. AdMob may collect non-personally identifiable device identifiers (such as Advertising ID) to serve relevant ads adhering to Google Play policies. If you register your email on our VIP Waitlist, it is used solely to notify you when paid subscriptions launch."
            )

            PolicySection(
                title = "4. Data Security",
                content = "All application preferences, overlay styles, and protected app configurations are stored locally on your device using encrypted DataStore storage. No personal data is sold or shared with third parties."
            )

            PolicySection(
                title = "5. Contact Us",
                content = "If you have any questions or privacy concerns regarding this policy, please contact our privacy compliance team at neuraai.apps@gmail.com."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Last updated: August 2026",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = content,
            fontSize = 14.sp,
            color = Color(0xFF475569),
            lineHeight = 20.sp
        )
    }
}
