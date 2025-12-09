# 🔍 DIAGNÓSTICO - Por qué no cargan los datos

## Problemas Identificados y Solucionados

### ✅ 1. **Timeouts Infinitos** (SOLUCIONADO)
**Problema:** Retrofit no tenía configurados timeouts, por lo que las peticiones podían esperar indefinidamente.

**Solución:** Agregué en `ApiService.kt`:
```kotlin
.connectTimeout(30, TimeUnit.SECONDS)
.readTimeout(30, TimeUnit.SECONDS)
.writeTimeout(30, TimeUnit.SECONDS)
```

---

### ✅ 2. **Sin Logging de Peticiones** (SOLUCIONADO)
**Problema:** No podías ver qué estaba pasando en las peticiones HTTP.

**Solución:** Agregué HttpLoggingInterceptor en `ApiService.kt`:
```kotlin
.addInterceptor(HttpLoggingInterceptor { message ->
    Log.d("OkHttp", message)
}.apply {
    level = HttpLoggingInterceptor.Level.BODY
})
```

---

### ✅ 3. **Logging Mejorado en ViewModel** (SOLUCIONADO)
**Problema:** El error no mostraba el stack trace completo.

**Solución:** Agregué en `MainViewModel.kt`:
```kotlin
Log.e("MainViewModel", "Stack trace:", e)
```

---

## 🔧 Cómo Diagnosticar el Problema

### **Paso 1: Ejecuta la app y abre Android Studio Logcat**
1. Abre **Logcat** en Android Studio (View → Tool Windows → Logcat)
2. Filtra por: `MainViewModel` y `OkHttp`
3. Ejecuta la app

### **Paso 2: Busca estos logs**

**Si ves esto, la petición se envía correctamente:**
```
D/OkHttp: --> GET /api/v1/comidas
D/OkHttp: <-- 200 OK
```

**Si ves esto, hay error de red:**
```
E/MainViewModel: Error HTTP: 404
E/MainViewModel: Error HTTP: 500
E/MainViewModel: Timeout al conectar
```

**Si ves esto, es problema de parsing JSON:**
```
E/MainViewModel: Tipo de error: JsonDataException
```

---

## 🎯 Posibles Causas Restantes

### **1. La API devuelve datos envueltos en un objeto**
Si el servidor devuelve:
```json
{
  "data": [
    { "id": 1, "nombre": "Hamburguesa", ... }
  ]
}
```

Pero tu interfaz espera:
```kotlin
@GET("comidas")
suspend fun getComidas(): List<ComidaDto>
```

**Solución:** Crea un DTO envolvente:
```kotlin
data class ComidaResponse(
    val data: List<ComidaDto>
)

@GET("comidas")
suspend fun getComidas(): ComidaResponse
```

---

### **2. El campo JSON no coincide con el modelo**
Si el servidor devuelve `imagen_url` pero tu modelo espera `imagenUrl`, Moshi puede fallar.

**Verificación:** Revisa que `@Json(name = "imagen_url")` esté en `Models.kt`

---

### **3. Problema de SSL/HTTPS**
Si el servidor tiene certificado autofirmado, Retrofit puede rechazar la conexión.

**Solución (solo para desarrollo):**
```kotlin
val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
})

val sslContext = SSLContext.getInstance("SSL").apply {
    init(null, trustAllCerts, SecureRandom())
}

val httpClient = OkHttpClient.Builder()
    .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
    .hostnameVerifier { _, _ -> true }
    .build()
```

---

## 📋 Checklist de Verificación

- [ ] ¿Tienes permiso `INTERNET` en `AndroidManifest.xml`? ✓ (Ya está)
- [ ] ¿La URL base es correcta? `https://fastfeast-apiv2.onrender.com/api/v1/`
- [ ] ¿El endpoint es correcto? `/comidas`
- [ ] ¿El servidor devuelve datos en cliente HTTP? (Confirma estructura JSON)
- [ ] ¿Hay error en los logs? (Revisa Logcat)

---

## 🚀 Próximos Pasos

1. **Ejecuta la app** y revisa los logs en Logcat
2. **Copia el error exacto** que ves en los logs
3. **Verifica la estructura JSON** que devuelve tu servidor en un cliente HTTP
4. **Comparte los logs** conmigo para diagnosticar mejor

---

## 📝 Cambios Realizados

**Archivos modificados:**
- `ApiService.kt` - Agregué timeouts y logging
- `MainViewModel.kt` - Mejoré el logging de errores

**Cambios no invasivos:** No cambié la lógica de negocio, solo agregué diagnóstico.
