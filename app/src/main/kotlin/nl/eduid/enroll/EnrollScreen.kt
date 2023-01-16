package nl.eduid.enroll

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import nl.eduid.R
import nl.eduid.ui.PrimaryButton
import nl.eduid.ui.theme.ButtonGreen
import nl.eduid.ui.theme.EduidAppAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollScreen(
    onLogin: () -> Unit,
    onScan: () -> Unit,
    onRequestEduId: () -> Unit
) = Scaffold { paddingValues ->

    ConstraintLayout(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .systemBarsPadding()
    ) {
        val (eduIdLogo, title, enrollLogo, requestEduIdButton, buttons) = createRefs()
        createVerticalChain(
            eduIdLogo,
            title,
            enrollLogo,
            buttons,
            requestEduIdButton,
            chainStyle = ChainStyle.Spread
        )

        Image(
            painter = painterResource(id = R.drawable.logo_eduid_big),
            contentDescription = "",
            modifier = Modifier
                .sizeIn(minWidth = 15.dp, minHeight = 59.dp)
                .constrainAs(eduIdLogo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )

        Text(
            text = stringResource(R.string.enroll_screen_title),
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .constrainAs(title) {
                    top.linkTo(eduIdLogo.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )

        Image(
            painter = painterResource(id = R.drawable.enroll_screen_logo),
            contentDescription = "",
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(enrollLogo) {
                    top.linkTo(title.bottom)
                    bottom.linkTo(buttons.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )

        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .constrainAs(buttons) {
                    bottom.linkTo(requestEduIdButton.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
        ) {

            PrimaryButton(
                text = "Sign In", onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            PrimaryButton(
                text = "Scan QR Code", onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
        }

        TextButton(
            onClick = onRequestEduId,
            modifier = Modifier
                .constrainAs(requestEduIdButton) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )
        {
            Text(
                text = "I don't have an eduID",
                style = MaterialTheme.typography.bodyLarge.copy(color = ButtonGreen),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
//    }
}

@Preview()
@Composable
private fun PreviewEnroll() {
    EduidAppAndroidTheme {
        EnrollScreen({}, {}, {})
    }
}




