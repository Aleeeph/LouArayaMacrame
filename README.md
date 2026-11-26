# Lou Araya Macramé · Catálogo Portátil e Interacción Comercial

App móvil Android (Kotlin + Jetpack Compose + Material 3) para el caso **Lou Araya Macramé SPA** de la asignatura DSY1105 Desarrollo de Aplicaciones Móviles. Centraliza el catálogo de piezas de macramé, las colecciones por campaña (matrimonios, festivales, decoración de interiores y viajes) y las solicitudes de reserva de los clientes.

> MVP académico con **datos ficticios o anonimizados** (Ley N.º 21.719 y Ley N.º 19.628). No contiene nombres, RUT ni correos reales.

## Integrantes

| Nombre | Alejandro Velasquez|
|
 **Sección:** DSY1105 · 

## Perfiles de usuario

| Perfil | Qué puede hacer |
|---|---|
| **Cliente / Planificador de eventos** | Explorar el catálogo, buscar y filtrar por campaña, guardar piezas en «Mi colección», compartir una pieza, solicitar una reserva (cantidad, fecha de evento, notas) y seguir el estado de sus solicitudes. |
| **Dueña / Administradora** | Crear, editar y eliminar piezas (con foto desde la cámara), ver todas las solicitudes y cambiar su estado (Exploración, Pendiente, Confirmado, Entregado), programar alertas de campaña. |

El perfil se elige al registrarse (en un producto real lo asignaría el backend).

## Datos (estructura sintética del caso)

- **Ficha de producto:** código correlativo (`MAC-001`), nombre del diseño, ancho × largo (cm), material (algodón, cáñamo, yute, lino, mezcla), precio ficticio en CLP, descripción e imagen.
- **Categoría de campaña:** Matrimonios, Festivales, Decoración de Interiores, Viajes.
- **Registro de pedido:** id, fecha, cliente anonimizado (`CLI-0001`), total, estado y notas.

La primera vez que se abre la app se carga una matriz de prueba (9 piezas y 3 campañas) en Room.

## Cumplimiento de la pauta

| Requisito | Dónde está |
|---|---|
| Interfaz organizada y navegación clara (IL2.1) | `ui/navigation/AppNavHost.kt` (barra inferior: Catálogo · Campañas · Solicitudes · Perfil), `ui/theme/Theme.kt` |
| Formularios validados con íconos y mensajes | `ui/components/FormComponents.kt`; formularios: Login, Registro, Pieza, Solicitud de reserva, Campaña |
| Validaciones desde la lógica | `domain/Validators.kt` (sin dependencias de UI) + tests en `app/src/test/.../ValidatorsTest.kt` |
| Gestión de estado (IL2.2) | `StateFlow` en los ViewModel + `collectAsStateWithLifecycle` + `remember`/`rememberSaveable` |
| Animaciones | Splash (spring + fade), entrada de tarjetas, corazón de favoritos, mensajes de error, barra inferior, overlay de éxito, cambio de color de campañas |
| MVVM + estructura modular + persistencia local (IL2.3) | Ver arquitectura; **Room** (usuarios, piezas, favoritos, pedidos, campañas) y **DataStore** (sesión y preferencias) |
| API REST con Retrofit | `data/remote/MacrameApi.kt` (endpoints), `Dtos.kt`, sincronización en `CatalogRepository` y `OrderRepository` |
| Recursos nativos (IL2.4) | **Cámara** para fotografiar piezas, **notificaciones** con WorkManager, compartir piezas y apertura de mapas externos para ferias artesanales. |
| Escáner QR | `ui/screens/QrScannerScreen.kt` + Google Code Scanner; lee códigos `MAC-001` y abre la ficha local de la pieza. |
| GitHub + Trello | Ver `docs/GUIA_EQUIPO.md` |

```
app/src/main/java/cl/louaraya/macrame/
├── data/
│   ├── local/        Room: entidades, DAOs, base de datos
│   ├── remote/       Retrofit: API REST, DTOs y cliente
│   ├── prefs/        DataStore: sesión, perfil, tema, notificaciones
│   └── repository/   Repositorios (única fuente de datos para los ViewModel)
├── domain/           Validators, reglas de pedido, catálogos y datos semilla
├── notifications/    WorkManager + canal y notificaciones de campaña
├── di/               Contenedor de dependencias manual
├── util/             Formatos, fotos (FileProvider), intents
├── viewmodel/        Estado de cada pantalla (StateFlow)
└── ui/               theme · components · navigation · screens
```

Flujo de datos: `Screen (Compose) → ViewModel (StateFlow) → Repository → Room / DataStore / API REST`.

## Conexión con el backend (Retrofit)

Room es la fuente de verdad, así que la app **funciona sin conexión**. Al abrir el catálogo (y con el botón de sincronizar) intenta descargar `GET productos`; las solicitudes se envían con `POST pedidos`. Si el servidor no responde, aparece un aviso de «sin conexión» y los cambios quedan guardados en el teléfono (los pedidos no sincronizados muestran un ícono de nube tachada).

1. Abre `app/build.gradle.kts` y cambia `API_BASE_URL` por la URL del backend de la cátedra (debe terminar en `/`). El valor por defecto `http://10.0.2.2:8080/api/` apunta al PC desde el emulador.
2. Si los nombres de campos JSON del backend son distintos, ajusta los `@SerializedName` en `data/remote/Dtos.kt` y las rutas en `MacrameApi.kt`.

## Cómo ejecutar

**Requisitos:** Android Studio (versión reciente), JDK 17 (incluido), emulador o dispositivo con Android 8.0+ (API 26).

1. Clona el repositorio y abre la carpeta `LouArayaMacrame` en Android Studio (**File ▸ Open**).
2. Espera a que termine **Gradle Sync** (requiere internet).
3. Selecciona un emulador o dispositivo y presiona **Run ▶**.
4. Tests unitarios: `./gradlew test` (o clic derecho en `ValidatorsTest` ▸ Run).

**Para probar la app:**
- Regístrate como **Dueña / Administradora** para crear piezas (cámara), cambiar estados y programar campañas; luego cierra sesión y regístrate como **Cliente** para guardar piezas y solicitar reservas.
- *Cámara:* en el emulador usa **Extended controls ▸ Camera** o un dispositivo físico.
- *Notificaciones:* acepta el permiso; al enviar una solicitud aparece un aviso inmediato. Las campañas se disparan en su fecha y hora (WorkManager puede retrasarlas unos minutos).

## Evaluación Parcial 4: cambios incorporados

- Se agregó una pantalla de escaneo QR accesible desde el Catálogo.
- Se utiliza Google Code Scanner para leer códigos sin implementar una cámara propia para el escáner.
- Los códigos `MAC-001`, `MAC-002`, etc. se interpretan como identificadores locales y navegan a la ficha del producto.
- Se agregaron pruebas unitarias para el reconocimiento y rechazo de códigos QR inválidos.
- Se conserva la captura fotográfica de productos y la apertura de mapas desde Perfil.

**Pendiente de configuración externa:** la URL Retrofit predeterminada sigue apuntando a `10.0.2.2:8080/api/`. Para demostrar integración real se debe levantar el backend Spring Boot compatible y configurar esa URL. Esta entrega no incorpora por sí sola un backend Spring Boot ni genera una firma APK; esos artefactos requieren configuración y ejecución en el entorno Android Studio del equipo.

## Notas de seguridad

- Las contraseñas no se guardan en texto plano (SHA-256 con el correo como sal). En un producto real se usaría un backend de autenticación.
- Las fotos se guardan en el almacenamiento interno y se comparten con la cámara mediante `FileProvider`.
- `usesCleartextTraffic` está activo solo para probar contra `http://10.0.2.2` en el emulador; no debe usarse en producción.
