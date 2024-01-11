package nl.eduid.screens.homepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import nl.eduid.R
import nl.eduid.ui.AlertDialogWithTwoButton
import nl.eduid.ui.PrimaryButtonWithIcon
import nl.eduid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageWithAccountContent(
    viewModel: HomePageViewModel,
    onActivityClicked: () -> Unit = {},
    onPersonalInfoClicked: () -> Unit = {},
    onSecurityClicked: () -> Unit = {},
    onScanForAuthorization: () -> Unit = {},
    launchOAuth: () -> Unit = {},
) = Scaffold(
    topBar = {
        CenterAlignedTopAppBar(
            modifier = Modifier.padding(top = 42.dp, start = 26.dp, end = 26.dp),
            title = {
                Image(
                    painter = painterResource(R.drawable.ic_correct_logo),
                    contentDescription = "",
                    modifier = Modifier.size(width = 122.dp, height = 46.dp),
                    alignment = Alignment.Center
                )
            },
        )
    },
) { paddingValues ->
    val isAuthorizedForDataAccess by viewModel.isAuthorizedForDataAccess.observeAsState(false)
    viewModel.uiState.promptForAuth?.let {
        AlertDialogWithTwoButton(title = stringResource(R.string.PromptForOAuth_Title_COPY),
            explanation = stringResource(id = R.string.PromptForOAuth_Description_COPY),
            dismissButtonLabel = stringResource(R.string.Button_Cancel_COPY),
            confirmButtonLabel = stringResource(R.string.PinAndBioMetrics_SignIn_COPY),
            onDismiss = viewModel::clearPromptForAuthTrigger,
            onConfirm = {
                viewModel.clearPromptForAuthTrigger()
                launchOAuth()
            })
    }
    AccountContent(
        paddingValues = paddingValues,
        isAuthorizedForDataAccess = isAuthorizedForDataAccess,
        onScanForAuthorization = onScanForAuthorization,
        onPersonalInfoClicked = onPersonalInfoClicked,
        promptAuthorization = viewModel::triggerPromptForAuth,
        onSecurityClicked = onSecurityClicked,
        onActivityClicked = onActivityClicked
    )
}

@Composable
private fun AccountContent(
    paddingValues: PaddingValues,
    isAuthorizedForDataAccess: Boolean,
    onScanForAuthorization: () -> Unit = {},
    onPersonalInfoClicked: () -> Unit = {},
    promptAuthorization: () -> Unit = {},
    onSecurityClicked: () -> Unit = {},
    onActivityClicked: () -> Unit = {},
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        val (title, bottomColumn) = createRefs()

        Text(
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextGreen)) {
                    append(stringResource(R.string.HomeView_MainText_FirstPart_COPY))
                }
                append("\n")
                withStyle(style = SpanStyle(color = TextBlack)) {
                    append(stringResource(R.string.HomeView_MainText_SecondPart_COPY))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    bottom.linkTo(bottomColumn.top)
                },
        )

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.constrainAs(bottomColumn) {
                bottom.linkTo(parent.bottom)
            },
        ) {
            Image(
                painter = painterResource(id = R.drawable.medidate_image),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 1.dp)
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SplashScreenBackgroundColor)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PrimaryButtonWithIcon(
                        text = stringResource(R.string.HomeView_ScanQRButton_COPY), onClick = {
                            onScanForAuthorization()
                        }, icon = R.drawable.homepage_scan_icon, modifier = Modifier.weight(1f)
                    )
                    PrimaryButtonWithIcon(
                        text = stringResource(R.string.HomeView_PersonalInfoButton_COPY), onClick = {
                            if (isAuthorizedForDataAccess) {
                                onPersonalInfoClicked()
                            } else {
                                promptAuthorization()
                            }
                        }, icon = R.drawable.homepage_info_icon, modifier = Modifier.weight(1f)
                    )

                    PrimaryButtonWithIcon(
                        text = stringResource(R.string.HomeView_SecurityButton_COPY), onClick = {
                            if (isAuthorizedForDataAccess) {
                                onSecurityClicked()
                            } else {
                                promptAuthorization()
                            }
                        }, icon = R.drawable.homepage_security_icon, modifier = Modifier.weight(1f)
                    )
                    PrimaryButtonWithIcon(
                        text = stringResource(R.string.HomeView_ActivityButton_COPY), onClick = {
                            if (isAuthorizedForDataAccess) {
                                onActivityClicked()
                            } else {
                                promptAuthorization()
                            }
                        }, icon = R.drawable.homepage_activity_icon, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewHomePageScreen() = EduidAppAndroidTheme {
    AccountContent(paddingValues = PaddingValues(), isAuthorizedForDataAccess = true)
}
