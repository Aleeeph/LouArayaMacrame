# Evaluación Parcial 4 — cambios en Lou Araya Macramé

## Funcionalidades incorporadas o conservadas

1. **Cámara:** se conserva la captura de fotografías de piezas desde `ProductFormScreen`, mediante `TakePicture`, permiso CAMERA y `FileProvider`.
2. **Escáner QR:** se incorpora `QrScannerScreen`, accesible desde el icono QR de la barra superior del Catálogo. Utiliza Google Code Scanner.
3. **Identificación de piezas:** `QrProductCode` reconoce códigos `MAC-001`, `MAC-002`, etc. El escáner consulta Room antes de navegar a la ficha; si el código no corresponde a una pieza existente, informa el resultado en pantalla.
4. **Mapas:** se conserva el acceso a ferias artesanales desde Perfil. Se intenta abrir una aplicación de mapas con un intent `geo:`; como alternativa se abre OpenStreetMap y luego Google Maps en navegador.
5. **Pruebas:** se agregan pruebas unitarias para reconocer códigos válidos, códigos incluidos en una URL y entradas no válidas.

## Cómo probar el escáner

- Ejecutar la app en un dispositivo con Google Play Services actualizado.
- Entrar a Catálogo y pulsar el icono QR de la barra superior.
- Escanear un QR cuyo contenido sea `MAC-001` (o el código de otra pieza que exista en Room).
- La app valida el código contra la base de datos local y abre la ficha de producto.
- Para un QR de prueba, se puede generar externamente un código cuyo texto sea `MAC-001`.

Google Code Scanner muestra su propia interfaz de escaneo y no requiere que la aplicación implemente una vista previa de cámara propia. El módulo puede descargarse mediante Google Play Services al primer uso.

## Configuración pendiente antes de la defensa

La evaluación completa exige más que cámara, QR y mapas. El ZIP recibido contiene la aplicación Android, pero no contiene un proyecto Spring Boot independiente. Antes de afirmar cumplimiento total de la pauta, el equipo debe:

- Levantar y entregar los microservicios Spring Boot con base de datos y endpoints CRUD operativos.
- Configurar `API_BASE_URL` en `app/build.gradle.kts` para apuntar al backend real. El valor actual `http://10.0.2.2:8080/api/` es solo una dirección de desarrollo para emulador.
- Verificar el contrato JSON del backend contra `data/remote/Dtos.kt` y `MacrameApi.kt`.
- Ejecutar la suite de pruebas y medir cobertura real de la lógica para acreditar el 80 % solicitado.
- Generar el APK release firmado desde Android Studio con un keystore propio y resguardar el `.jks` de forma segura. No incluir contraseñas de firma en Git.
- Adjuntar evidencia de Trello y commits de ambos integrantes.

## Pruebas unitarias agregadas

`app/src/test/java/cl/louaraya/macrame/QrProductCodeTest.kt`

La existencia de estas pruebas no demuestra por sí sola el 80 % de cobertura global; esa cifra debe medirse con el reporte de cobertura del proyecto.
