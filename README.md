# ComuniK

Aplicación móvil Android de accesibilidad para facilitar la comunicación de personas con perdida sensorial del habla, mediante captura de gestos de la mano (lengua de señas), voz y conversión a texto.

ComuniK es una aplicación  que permite:
- Autenticación de usuarios (Login, Registro, Recuperar contraseña)
- Almacenamiento local de hasta 5 usuarios
- Captura de gestos básicos mediante la cámara del dispositivo
- Conversión rapida de texto a voz
- Conversión básica de gestos a texto visible en pantalla o voz

## Tecnologías

Android Studio
Jetpack Compose
Material Design 3
Kotlin

Proyecto desarrollado para la asignatura Desarrollo de Aplicaciones Móviles.

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

## Consideraciones finales

La aplicación utiliza una arquitectura simple con una sola Activity y Jetpack Compose, facilitando la incorporación de nuevas funcionalidades.

El código implementa características avanzadas de Kotlin (funciones de orden superior, inline, lambdas, extensiones y manejo de excepciones) y mantiene una separación clara entre capas de datos, interfaz y utilidades.
