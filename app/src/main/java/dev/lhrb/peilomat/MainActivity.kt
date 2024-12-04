package dev.lhrb.peilomat

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import dev.lhrb.peilomat.ui.theme.PeilomatTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location -> Log.d("Location", "last location $location")
        }

        enableEdgeToEdge()
        setContent {
            PeilomatTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Peilomat 3000")
                            }
                        )

                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CheckLocationPermission()
                    Greeting(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CheckLocationPermission() {
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val currentPermissionState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (PackageManager.PERMISSION_GRANTED != currentPermissionState) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val lat = 50.935173
    val lon = 6.953101
    val RW = "3 84 000"
    val HW = "55 67 999"
    Column(modifier = modifier.padding(8.dp)) {
        CurrentPosition(lat, lon, RW, HW)

        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 12.dp)
        )

        TransformRWHWtoLatLon()

        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 12.dp)
        )
    }
}

@Composable
fun TransformAngleAndDistanceToLatLon() {

}

@Composable
fun TransformRWHWtoLatLon() {
    var rw by remember { mutableStateOf("") }
    var hw by remember { mutableStateOf("") }

    Column {
        Text("Convert RW,HW to Lat,Lon")

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {

            OutlinedTextField(
                value = rw,
                onValueChange = { rw = it },
                label = { Text("RW") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)

            )

            OutlinedTextField(
                value = hw,
                onValueChange = { hw = it },
                label = { Text("HW") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("lat,lon") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Convert")
        }
    }
}


@Composable
fun CurrentPosition(
    lat: Double,
    lon: Double,
    rW: String,
    hW:String
) {
    Column() {
        Text("Aktuelle Position:")

        OutlinedTextField(
            value = "$lat,$lon",
            onValueChange = {},
            label = { Text("lat,lon") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {

            OutlinedTextField(
                value = rW,
                onValueChange = {},
                label = { Text("RW") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)

            )

            OutlinedTextField(
                value = hW,
                onValueChange = {},
                label = { Text("HW") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PeilomatTheme {
        Greeting()
    }
}