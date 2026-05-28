# MANUAL DE USUARIO - SISTEMA SIGOMEI

## Bienvenido al Sistema de Gestión de Mantenimiento
Esta guía le ayudará a navegar por la interfaz gráfica del cliente para gestionar sus equipos industriales de manera eficiente.

## 1. Pantalla de Registro de Equipos
En esta pestaña podrá dar de alta nueva maquinaria:
- **Campos:** Nombre, Marca, Modelo, N/S y Ubicación.
- **Selectores:** Utilice las listas desplegables para elegir el "Tipo de Equipo" y su "Criticidad".
- **Botón:** "GUARDAR EQUIPO". Al presionarlo, el sistema enviará la información al servidor central.

## 2. Pantalla de Registro de Técnicos
Utilice esta sección para gestionar su personal:
- **Validación:** Asegúrese de ingresar un RFC válido de 13 caracteres.
- **Especialidad:** Debe coincidir con los tipos de equipo para poder asignar órdenes después.
- **Botón:** "REGISTRAR TÉCNICO".

## 3. Pantalla de Órdenes de Mantenimiento
Aquí se realiza la programación de los servicios:
- **IDs:** Debe ingresar el ID numérico del equipo y del técnico (puede consultarlos en la pestaña de Visualización).
- **Reglas de Negocio:** El sistema le notificará si intenta asignar un técnico inactivo o si la especialidad no coincide con el equipo (Reglas RN-01 a RN-08).
- **Botón:** "GENERAR ORDEN".

## 4. Visualización de Datos (Tablas)
Esta es la herramienta de consulta principal:
- **Tablas:** Visualice en tiempo real Equipos, Técnicos y Órdenes guardadas.
- **Actualización:** Presione el botón **"REFRESCAR DATOS"** para obtener la información más reciente desde el servidor central.

## 5. Consola de Estado (Terminal Inferior)
Ubicada en la parte inferior de la ventana, esta consola le brinda retroalimentación inmediata del servidor:
- **Texto Cian:** Indica que la operación fue exitosa.
- **Texto de Error:** Le indicará exactamente qué regla de negocio se rompió si una operación fue rechazada.

---
**SIGOMEI v1.0** - Soluciones de Mantenimiento en Red.
