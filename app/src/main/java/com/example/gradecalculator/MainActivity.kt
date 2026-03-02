package com.example.gradecalculator

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gradecalculator.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradeCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GradeCalculatorScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCalculatorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            errorMessage = null
            scope.launch {
                val result = readExcelFile(context.contentResolver.openInputStream(it))
                if (result.isSuccess) {
                    students = result.getOrNull() ?: emptyList()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to read Excel file"
                }
                isLoading = false
            }
        }
    }

    val fileSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = writeExcelFile(context.contentResolver.openOutputStream(it), students)
                if (result.isSuccess) {
                    Toast.makeText(context, "Grades exported successfully!", Toast.LENGTH_LONG).show()
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Unknown error"
                    Toast.makeText(context, "Export failed: $msg", Toast.LENGTH_LONG).show()
                    Log.e("GradeCalculator", "Export failed", result.exceptionOrNull())
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Grade Calculator",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    if (students.isNotEmpty()) {
                        IconButton(onClick = { fileSaverLauncher.launch("Calculated_Grades.xlsx") }) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download Results",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = "Import Excel")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (students.isEmpty() && !isLoading && errorMessage == null) {
                EmptyState()
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (students.isNotEmpty()) {
                        SummaryHeader(students)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage?.let {
                        ErrorMessage(it)
                    }

                    AnimatedVisibility(
                        visible = students.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(students) { student ->
                                StudentGradeCard(student)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryHeader(students: List<Student>) {
    val average = students.map { it.score }.average()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Students", style = MaterialTheme.typography.labelMedium)
                Text(
                    students.size.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(modifier = Modifier.height(30.dp).width(1.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Avg Score", style = MaterialTheme.typography.labelMedium)
                Text(
                    String.format("%.1f", average),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StudentGradeCard(student: Student) {
    val gradeColor = getGradeColor(student.grade)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Score: ${student.score.toInt()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(gradeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.grade,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = gradeColor
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No data available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            "Import an Excel file to see results",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center
        )
    }
}

fun getGradeColor(grade: String): Color {
    return when (grade) {
        "A" -> GradeA
        "B" -> GradeB
        "C" -> GradeC
        "D" -> GradeD
        else -> GradeF
    }
}

suspend fun readExcelFile(inputStream: InputStream?): Result<List<Student>> = withContext(Dispatchers.IO) {
    try {
        if (inputStream == null) return@withContext Result.failure(Exception("Could not open file"))

        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val studentList = mutableListOf<Student>()

        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val nameCell = row.getCell(0)
            val scoreCell = row.getCell(1)

            val name = nameCell?.toString() ?: "Unknown"
            val score = try {
                scoreCell?.numericCellValue ?: 0.0
            } catch (e: Exception) {
                0.0
            }

            studentList.add(Student(name, score, calculateGrade(score)))
        }
        workbook.close()
        Result.success(studentList)
    } catch (e: Exception) {
        Log.e("GradeCalculator", "Error reading Excel", e)
        Result.failure(e)
    }
}

suspend fun writeExcelFile(outputStream: OutputStream?, students: List<Student>): Result<Unit> = withContext(Dispatchers.IO) {
    var workbook: XSSFWorkbook? = null
    try {
        if (outputStream == null) return@withContext Result.failure(Exception("Output error"))
        
        workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Calculated Grades")

        // Styles
        val headerFont = workbook.createFont()
        headerFont.bold = true
        
        val headerStyle = workbook.createCellStyle()
        headerStyle.alignment = HorizontalAlignment.CENTER
        headerStyle.setFont(headerFont)
        
        val centerStyle = workbook.createCellStyle()
        centerStyle.alignment = HorizontalAlignment.CENTER

        val scoreStyle = workbook.createCellStyle()
        scoreStyle.alignment = HorizontalAlignment.CENTER
        scoreStyle.dataFormat = workbook.createDataFormat().getFormat("0")

        // Create Header Row
        val headerRow = sheet.createRow(0)
        val headers = listOf("Student Name", "Score", "Grade")
        headers.forEachIndexed { i, title ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(title)
            cell.cellStyle = headerStyle
        }

        // Fill Data
        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            
            val nameCell = row.createCell(0)
            nameCell.setCellValue(student.name)
            nameCell.cellStyle = centerStyle
            
            val scoreCell = row.createCell(1)
            scoreCell.setCellValue(student.score)
            scoreCell.cellStyle = scoreStyle
            
            val gradeCell = row.createCell(2)
            gradeCell.setCellValue(student.grade)
            gradeCell.cellStyle = centerStyle
        }

        // Set fixed column widths
        sheet.setColumnWidth(0, 25 * 256) // Increased to ensure names fit
        sheet.setColumnWidth(1, 15 * 256) // Increased to ensure "Score" header fits fully
        sheet.setColumnWidth(2, 10 * 256)

        outputStream.use { 
            workbook.write(it)
        }
        Result.success(Unit)
    } catch (t: Throwable) {
        Log.e("GradeCalculator", "Error exporting Excel", t)
        Result.failure(t)
    } finally {
        try {
            workbook?.close()
        } catch (e: Exception) {
            // Ignore
        }
    }
}

fun calculateGrade(score: Double): String {
    return when {
        score >= 85 -> "A"
        score >= 70 -> "B"
        score >= 50 -> "C"
        score >= 40 -> "D"
        else -> "F"
    }
}
