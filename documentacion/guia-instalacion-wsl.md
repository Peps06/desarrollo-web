# Guía de instalación del entorno en WSL (HU-02)

Guía común para los 5 integrantes. Probar primero en el WSL de Alfonso y luego cada quien la sigue en el suyo.
Esta guía es la base de la sección de instalación del README final (HU-41).

> Se asume **WSL2 con Ubuntu 22.04 o superior**. Todos los comandos se ejecutan en la terminal de Ubuntu, no en PowerShell.

## 0. Consejos previos

- Trabaja el repositorio **dentro del sistema de archivos de Linux** (por ejemplo `~/proyectos/desarrollo-web`), **no** en `/mnt/c/...`: es mucho más rápido y evita problemas de permisos con Maven y Git.
- Actualiza los paquetes antes de instalar:

```bash
sudo apt update && sudo apt upgrade -y
```

## 1. Instalar JDK 17, Maven, MySQL Server y Git

```bash
sudo apt install -y openjdk-17-jdk maven mysql-server git
```

> El proyecto se ejecuta con el wrapper (`./mvnw`), pero Maven global sirve para el primer arranque y para comprobar el entorno.

## 2. Verificar las versiones

Cada integrante debe poder mostrar la salida correcta de estos 4 comandos (esa captura va a `docs/evidencias/`):

```bash
java -version      # debe indicar 17 o superior
mvn -v             # Apache Maven 3.x y Java 17+
mysql --version    # mysql  Ver 8.x
git --version
```

## 3. Iniciar MySQL (⚠ problema típico de WSL)

En WSL, **MySQL no arranca solo al abrir la terminal**. Cada vez que abras Ubuntu (o reinicies la PC) hay que iniciar el servicio:

```bash
sudo service mysql start
sudo service mysql status      # debe decir "active" / "running"
```

Si falla al arrancar con el mensaje `su: warning: cannot change directory to /nonexistent`:

```bash
sudo service mysql stop
sudo usermod -d /var/lib/mysql/ mysql
sudo service mysql start
```

Si al correr el backend aparece `Communications link failure` o `Connection refused`, casi siempre es que MySQL está apagado: ejecuta `sudo service mysql start`.

## 4. Crear el esquema y el usuario de la aplicación

Entra a MySQL como administrador (en Ubuntu, `sudo mysql` funciona sin contraseña):

```bash
sudo mysql
```

Dentro de MySQL, crea el esquema vacío (`utf8mb4`) y un usuario con permisos **solo** sobre ese esquema:

```sql
CREATE DATABASE bitacora_viajes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'bitacora_app'@'localhost' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE';
GRANT ALL PRIVILEGES ON bitacora_viajes.* TO 'bitacora_app'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

Comprueba que el usuario de la aplicación puede entrar (te pide la contraseña que escribiste arriba):

```bash
mysql -u bitacora_app -p
```

```sql
SHOW DATABASES;               -- debe aparecer bitacora_viajes
USE bitacora_viajes;
SHOW TABLES;                  -- vacío hasta que corra el backend (HU-07)
```

> Usa una contraseña de desarrollo (no una real) y **no** la subas a GitHub. Se escribe solo en tu `application.properties` local.

## 5. Configurar Git y acceso a GitHub desde WSL

```bash
git config --global user.name  "Tu Nombre"
git config --global user.email "tu-correo@ejemplo.com"
git config --global init.defaultBranch main
git config --global --list
```

### Opción A: llave SSH (recomendada)

```bash
ssh-keygen -t ed25519 -C "tu-correo@ejemplo.com"     # Enter a todo
cat ~/.ssh/id_ed25519.pub                             # copia todo el contenido
```

En GitHub: **Settings → SSH and GPG keys → New SSH key** y pega la llave. Luego prueba:

```bash
ssh -T git@github.com        # "Hi <usuario>! You've successfully authenticated..."
```

Clonar por SSH:

```bash
mkdir -p ~/proyectos && cd ~/proyectos
git clone git@github.com:Peps06/desarrollo-web.git
```

### Opción B: token personal (HTTPS)

En GitHub: **Settings → Developer settings → Personal access tokens** (scope `repo`). Al hacer `git push` por HTTPS, usa tu usuario y el token como contraseña.

```bash
git clone https://github.com/Peps06/desarrollo-web.git
```

## 6. Prueba final del entorno

1. `git clone` del repositorio en tu WSL.
2. Crea tu rama de prueba y súbela (también es la prueba de HU-01):

```bash
cd desarrollo-web
git checkout -b feature/hu-01-prueba-tunombre
echo "Prueba de <tu nombre>" >> docs/pruebas-git.txt
git add docs/pruebas-git.txt
git commit -m "HU-01: prueba de flujo Git de <tu nombre>"
git push -u origin feature/hu-01-prueba-tunombre
```

3. Abre el pull request en GitHub y pide revisión a otro integrante.

## Lista de verificación por integrante

| Verificación | Alfonso | Stephy | Dana | Diego | Citlaly |
|---|:-:|:-:|:-:|:-:|:-:|
| `java -version` (17+) | ☐ | ☐ | ☐ | ☐ | ☐ |
| `mvn -v` | ☐ | ☐ | ☐ | ☐ | ☐ |
| `mysql --version` | ☐ | ☐ | ☐ | ☐ | ☐ |
| `git --version` | ☐ | ☐ | ☐ | ☐ | ☐ |
| MySQL activo y `mysql -u bitacora_app -p` entra | ☐ | ☐ | ☐ | ☐ | ☐ |
| Esquema `bitacora_viajes` (utf8mb4) creado | ☐ | ☐ | ☐ | ☐ | ☐ |
| `git push` de prueba desde WSL | ☐ | ☐ | ☐ | ☐ | ☐ |
