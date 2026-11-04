package com.rgs.iagerador

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00F3FF),
                    background = Color(0xFF0A0E17),
                    surface = Color(0xFF111827),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TelaGeradorRGS()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGeradorRGS() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var modoAdulto by remember { mutableStateOf(false) }
    var imagemGaleria by remember { mutableStateOf<Uri?>(null) }
    var idadeConfirmada by remember { mutableStateOf(false) }
    var exibirDialogoIdade by remember { mutableStateOf(false) }
    var estaGerando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoGeracao?>(null) }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imagemGaleria = uri }

    if (exibirDialogoIdade) {
        AlertDialog(
            onDismissRequest = { exibirDialogoIdade = false },
            title = { Text("Confirmação de Idade (+18)", fontWeight = FontWeight.Bold, color = Color(0xFFFF0055)) },
            text = { Text("Você confirma que tem 18 anos ou mais para ativar o Modo Adulto?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        idadeConfirmada = true
                        modoAdulto = true
                        exibirDialogoIdade = false
                        Toast.makeText(context, "Modo Adulto ativado", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F3FF))
                ) { Text("Confirmar +18", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { modoAdulto = false; exibirDialogoIdade = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF111827)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RGS IA • Gerador",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00F3FF),
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )

        // Switch Modo Adulto
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Conteúdo Adulto", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        text = if (modoAdulto) "LIGADO (+18)" else "DESLIGADO (Seguro)",
                        fontSize = 13.sp,
                        color = if (modoAdulto) Color(0xFFFF0055) else Color(0xFF00FF88)
                    )
                }
                Switch(
                    checked = modoAdulto,
                    onCheckedChange = { ligado ->
                        if (ligado && !idadeConfirmada) exibirDialogoIdade = true
                        else modoAdulto = ligado
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF0055),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        // Galeria
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { launcherGaleria.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) { Text("📷 Imagem de Referência", color = Color(0xFF00F3FF)) }

                imagemGaleria?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, Color(0xFF00F3FF), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    TextButton(onClick = { imagemGaleria = null }) {
                        Text("Remover", color = Color(0xFFFF5555))
                    }
                }
            }
        }

        // Prompt
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("O que você quer gerar?") },
            placeholder = { Text("Descreva a cena, estilo, iluminação...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            maxLines = 5,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF00F3FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedLabelColor = Color(0xFF00F3FF),
                cursorColor = Color(0xFF00F3FF)
            )
        )

        // Botão principal
        Button(
            onClick = {
                if (prompt.isBlank()) {
                    Toast.makeText(context, "Digite um prompt", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (modoAdulto && imagemGaleria != null) {
                    Toast.makeText(context, "Bloqueado: foto real + Modo Adulto não permitido", Toast.LENGTH_LONG).show()
                    return@Button
                }

                estaGerando = true
                resultado = null

                scope.launch {
                    resultado = gerarImagemEVideo(prompt, imagemGaleria, modoAdulto)
                    estaGerando = false
                }
            },
            enabled = !estaGerando,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (modoAdulto) Color(0xFFFF0055) else Color(0xFF00F3FF)
            )
        ) {
            if (estaGerando) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Gerando...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Text("🚀 Gerar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Resultado
        AnimatedVisibility(visible = resultado != null) {
            resultado?.let { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Resultado", fontWeight = FontWeight.Bold, color = Color(0xFF00F3FF))

                        res.imagemUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Resultado gerado",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(res.mensagem, fontSize = 14.sp, color = Color.LightGray)

                        if (!res.sucesso) {
                            Text("Tente novamente ou ajuste o prompt.", color = Color(0xFFFF5555), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

data class ResultadoGeracao(
    val sucesso: Boolean,
    val mensagem: String,
    val imagemUrl: String? = null
)

/**
 * Função de geração (atualmente simulada).
 * Substitua o corpo por chamada real (Replicate, Fal.ai, Stability, etc.).
 */
suspend fun gerarImagemEVideo(
    prompt: String,
    imagemBase: Uri?,
    adulto: Boolean
): ResultadoGeracao {
    delay(2000)

    val modo = if (adulto) "Modo Adulto" else "Modo Seguro"
    val origem = if (imagemBase != null) "com imagem de referência" else "texto apenas"

    // Placeholder visual (troque pela URL real da API)
    val url = "https://picsum.photos/800/600?random=${(0..999).random()}"

    return ResultadoGeracao(
        sucesso = true,
        mensagem = "Gerado com sucesso ($origem).\n\nPrompt: \"$prompt\"\nModo: $modo",
        imagemUrl = url
    )
}