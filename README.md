Este repositorio contiene la versión evolucionada de la aplicación Dados en Jetpack Compose. Para garantizar un desarrollo ordenado, minimizar conflictos en Git (merge conflicts) y construir una arquitectura limpia desde la base, las 19 Historias de Usuario (HU) están organizadas en 4 Fases Secuenciales de Integración.

Hoja de Ruta de Desarrollo (Roadmap)

[FASE 1: Infraestructura & Navegación]
[FASE 2: Núcleo de Juego & Estado]                                                         │
[FASE 3: UI/UX & Personalización]
[FASE 4: Sensores, Audio & Hardware] 


📌 Fase 1: Arquitectura Base, Navegación y Persistencia Inicial
Objetivo: Establecer el esqueleto de la aplicación, el flujo de pantallas y la configuración de datos locales.

HU-17: Navegación a pantalla de "Acerca de"

Configurar Navigation Compose para definir las rutas principales de la app. Es la base sobre la cual se insertarán las demás vistas.

HU-01: Pantalla de Bienvenida (Splash Screen)

Implementar el punto de entrada de la app y la transición hacia la pantalla principal definida en la navegación.

HU-12: Guardar el nombre del jugador

Configurar la integración con DataStore o SharedPreferences para el manejo de persistencia local simple desde la primera sesión.

🎲 Fase 2: Lógica del Juego y Gestión de Estado
Objetivo: Expandir las mecánicas centrales de la aplicación aprovechando el estado mutable en Compose.

HU-08: Contador de tiradas consecutivas

Crear el estado global/local para contar los lanzamientos de la sesión actual.

HU-06: Modo de dos dados simultáneos

Adaptar la interfaz para soportar múltiples dados y el cálculo sumatorio.

HU-10: Fijar/Bloquear dado (Hold)

Añadir lógica de selección sobre la estructura de múltiples dados desarrollada en la tarea anterior.

HU-07: Configuración del tipo de dado (D4, D6, D8, D12, D20)

Generalizar la lógica de aleatoriedad y renderizado para soportar distintos rangos numéricos.

HU-09: Indicador de tiro afortunado (Crítico / Pifia)

Implementar validaciones condicionales sobre los resultados de los dados.

HU-11: Historial de las últimas tiradas

Crear la estructura de datos para almacenar una lista de resultados e integrarla con LazyColumn / LazyRow.

HU-13: Estadísticas básicas de tiradas

Procesar los datos generados por el historial para calcular y mostrar métricas en tiempo real.

HU-14: Límite de lanzamientos y victoria

Definir reglas de cierre de juego basadas en la acumulación de puntos o límite de turnos.

🎨 Fase 3: Experiencia Visual, Animaciones y Personalización
Objetivo: Mejorar la interfaz de usuario (UI) y la interacción estética sin modificar la lógica base.

HU-03: Selector de Tema Claro / Oscuro manual

Controlar el estado global de MaterialTheme en toda la jerarquía de Composables.

HU-04: Selección de color de dados

Implementar dynamic tinting o cambio de asset sets sobre los dados activos.

HU-05: Modificador de tamaño de dado

Integrar controles Slider asociados a los modificadores de dimensión (Modifier.size).

HU-02: Animación al lanzar el dado

Aplicar transformaciones de rotación o escala (animateFloatAsState) durante el evento de tiro.

📱 Fase 4: Integración con Hardware, Multimedia y Sistema
Objetivo: Agregar capacidades nativas avanzadas del dispositivo Android.

HU-15: Efectos de sonido al lanzar

Integrar SoundPool o MediaPlayer vinculados al ciclo de vida del lanzamiento.

HU-16: Feedback háptico (Vibración)

Hacer uso de los servicios de vibración del sistema al finalizar la animación de tiro.

HU-18: Compartir resultado

Implementar Intents explícitos de Android (ACTION_SEND) para comunicarse con aplicaciones externas.

HU-19: Lanzar agitando el dispositivo (Sensor)

Conectar la recepción de datos del acelerómetro (SensorManager) con la acción de tiro.

🛠️ Buenas Prácticas para el Equipo
Cada estudiante debe crear una rama Git partiendo de develop con el formato feature/HU-XX-descripcion.

No modificar la firma de los componentes principales compartidos sin previa discusión en el repositorio.

Realizar Pull Requests (PR) respetando el orden estricto estipulado en este README.
