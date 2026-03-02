# ComuniK

Aplicación móvil Android de accesibilidad para facilitar la comunicación de personas con discapacidad sensorial del habla, mediante escritura de texto, reconocimiento de voz y geolocalización.

ComuniK es una aplicación que permite:
- Autenticación de usuarios con Firebase (Login, Registro, Recuperar contraseña)
- Escritura de textos con guardado en Firebase Storage
- Reconocimiento de voz usando RecognitionListener de Android
- Text-to-Speech para convertir texto a voz
- Geolocalización para buscar y guardar ubicaciones de dispositivos
- Almacenamiento en la nube con Firebase Storage

## Tecnologías

- **Android Studio**: IDE de desarrollo
- **Jetpack Compose**: Framework de UI declarativo
- **Material Design 3**: Sistema de diseño
- **Navigation Compose**: Navegación entre pantallas
- **Kotlin**: Lenguaje de programación
- **Firebase Authentication**: Autenticación de usuarios
- **Firebase Storage**: Almacenamiento de archivos
- **SpeechRecognizer**: Reconocimiento de voz (RecognitionListener)
- **TextToSpeech**: Conversión de texto a voz
- **FusedLocationProviderClient**: Geolocalización

Proyecto desarrollado para la asignatura Desarrollo de Aplicaciones Móviles.

## Estructura del Proyecto

```
app/src/main/java/com/example/comunik/
├── data/
│   ├── FirebaseAuthService.kt          # Servicio de autenticación Firebase
│   ├── FirebaseStorageService.kt        # Servicio de almacenamiento Firebase
│   ├── AuthRepository.kt                # Repositorio de autenticación
│   ├── models/                          # Modelos de datos
│   └── services/                        # Servicios CRUD
│       ├── UserService.kt
│       ├── TextService.kt
│       ├── SpeechService.kt
│       ├── DeviceService.kt
│       └── PhrasesService.kt
├── ui/
│   ├── screens/                         # Pantallas de la aplicación
│   ├── components/                      # Componentes reutilizables
│   ├── widgets/                         # Widgets personalizados
│   └── providers/                       # Content Providers
└── MainActivity.kt                      # Actividad principal y navegación
```

## Pantallas

- **LoginScreen**: Inicio de sesión
- **RegisterScreen**: Registro de usuarios
- **ForgotPasswordScreen**: Recuperación de contraseña
- **HomeScreen**: Pantalla principal con acceso a funcionalidades
- **WriteScreen**: Escritura de textos con guardado en Firebase Storage
- **SpeakScreen**: Reconocimiento de voz y Text-to-Speech
- **FindDeviceScreen**: Geolocalización y guardado de ubicaciones
- **PhrasesScreen**: Visualización y gestión de frases guardadas

## Servicios CRUD

La aplicación incluye servicios para gestionar datos:

- **UserService**: Gestión de usuarios
- **TextService**: CRUD de textos guardados
- **SpeechService**: CRUD de audios/speeches
- **DeviceService**: CRUD de dispositivos/ubicaciones
- **PhrasesService**: Gestión de frases guardadas localmente

## Permisos Requeridos

La aplicación requiere los siguientes permisos:

- `INTERNET`: Conexión a Firebase
- `ACCESS_FINE_LOCATION`: Geolocalización precisa
- `ACCESS_COARSE_LOCATION`: Geolocalización aproximada
- `RECORD_AUDIO`: Reconocimiento de voz

## Pruebas

La aplicación incluye pruebas unitarias con:
- **JUnit**: Pruebas de lógica de negocio
- **Mockito**: Mocking de servicios
- **Robolectric**: Pruebas de componentes UI
- **Espresso**: Pruebas de UI end-to-end
- **Firebase Test Lab**: Pruebas de instrumentación en la nube

## Características Técnicas

### 1. Funciones de orden superior
**Ubicación:**  
`app/src/main/java/com/example/comunik/ui/screens/HomeScreen.kt`

**Descripción:**  
Se utiliza una función de orden superior en el método `processQuickActions`, el cual recibe una función de transformación como parámetro para procesar dinámicamente la lista de acciones rápidas de la pantalla principal. Esta función permite aplicar transformaciones personalizadas a cada acción (como modificar títulos, filtrar elementos u otras transformaciones) antes de mostrarlas en la interfaz de usuario.

### 2. Filter
**Ubicación:**  
`app/src/main/java/com/example/comunik/data/AuthRepository.kt`

**Descripción:**  
La función `filter` se emplea en el método `filterUsers` para generar una nueva lista de usuarios según criterios de nombre y email. Este proceso no modifica la lista original, permitiendo un manejo seguro del estado. La función filtra los usuarios nulos y aplica los criterios de búsqueda de forma case-insensitive.

### 3. Funciones inline
**Ubicación:**  
`app/src/main/java/com/example/comunik/util/SafeRun.kt`

**Descripción:**  
La función `safeRun` está declarada como `inline` para optimizar el uso de lambdas pequeñas y repetitivas. Su objetivo es ejecutar bloques de código de forma segura y eficiente, capturando excepciones automáticamente.

### 4. Lambdas
**Ubicación:**  
- `app/src/main/java/com/example/comunik/ui/screens/HomeScreen.kt`  
- `app/src/main/java/com/example/comunik/data/AuthRepository.kt`
- `app/src/main/java/com/example/comunik/MainActivity.kt`

**Descripción:**  
Se utilizan lambdas para definir comportamientos dinámicos, como las transformaciones aplicadas en las funciones de orden superior y el manejo de eventos en la interfaz de usuario mediante Jetpack Compose (onClick, onValueChange, etc.).

### 5. Lambda con etiqueta
**Ubicación:**  
`app/src/main/java/com/example/comunik/data/AuthRepository.kt`

**Descripción:**  
Se aplica una lambda con etiqueta dentro del proceso de filtrado en el método `filterUsers` para controlar el flujo de ejecución sin salir del método principal, permitiendo evaluar múltiples condiciones de forma clara y controlada.

### 6. Funciones de extensión
**Ubicación:**  
`app/src/main/java/com/example/comunik/util/Extensions.kt`

**Descripción:**  
Se definen funciones de extensión para agregar comportamiento a tipos existentes, como la validación de formato de email (`isValidEmail`), el formateo de emails (`formatEmail`) y la generación de texto descriptivo para los usuarios (`getDisplayInfo`), sin modificar las clases originales.

### 7. Propiedades de extensión
**Ubicación:**  
`app/src/main/java/com/example/comunik/util/Extensions.kt`

**Descripción:**  
Se implementan propiedades de extensión como `esValido` para usuarios e `esVacio` para strings, que indican estados o validaciones. Estas propiedades no almacenan estado, sino que calculan su valor dinámicamente.

### 8. Excepciones
**Ubicación:**  
- `app/src/main/java/com/example/comunik/data/exceptions/AuthExceptions.kt`
- `app/src/main/java/com/example/comunik/data/AuthRepository.kt`

**Descripción:**  
Se utilizan excepciones para manejar situaciones inválidas en la lógica de negocio, como credenciales inválidas, usuarios ya existentes, límites alcanzados y datos de usuario inválidos en el repositorio de autenticación, evitando que la aplicación continúe en un estado inconsistente.

### 9. Try / Catch
**Ubicación:**  
- `app/src/main/java/com/example/comunik/util/SafeRun.kt`  
- `app/src/main/java/com/example/comunik/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/example/comunik/MainActivity.kt`

**Descripción:**  
El manejo de errores se realiza mediante bloques `try/catch`, encapsulados en la función `safeRun`, permitiendo capturar excepciones y mostrar resultados controlados en la interfaz sin que la aplicación falle.

---

## Arquitectura

La aplicación utiliza una arquitectura simple con una sola Activity y Jetpack Compose, facilitando la incorporación de nuevas funcionalidades. El código implementa características avanzadas de Kotlin (funciones de orden superior, inline, lambdas, extensiones y manejo de excepciones) y mantiene una separación clara entre capas de datos, interfaz y utilidades.

### Almacenamiento

La aplicación utiliza Firebase Storage para almacenar archivos (textos, audios) y metadatos, permitiendo sincronización en la nube y acceso desde múltiples dispositivos.

## Consideraciones finales

El proyecto demuestra el uso de tecnologías modernas de Android y características avanzadas de Kotlin, manteniendo un código limpio y bien estructurado para facilitar el mantenimiento y la extensión de funcionalidades.
