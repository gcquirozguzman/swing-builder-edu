# Logo de Cibertec

Deja aquí el logo. Estos son los nombres que el plugin va a buscar:

| Archivo | Para qué | Tamaño sugerido |
|---|---|---|
| `cibertec.svg` | Logo horizontal (diálogo de formulario nuevo, cabecera de la paleta) | ~120 x 24 |
| `cibertec_dark.svg` | El mismo, para tema oscuro del IDE | igual |
| `cibertec_16.svg` | Marca pequeña (icono de archivo `.sbe`, listas) | 16 x 16 |
| `cibertec_16_dark.svg` | El mismo, para tema oscuro | 16 x 16 |

## Por qué SVG y no PNG

IntelliJ escala los iconos según el zoom del IDE y la densidad de la pantalla. Un PNG
se ve borroso en pantallas HiDPI; un SVG no. Si solo tienes PNG, que sea **el doble**
del tamaño de la tabla (por ejemplo 32x32 para el de 16) y añade también la versión
`@2x`.

## Lo del sufijo `_dark`

Es una convención de la plataforma, no un capricho: si existe `cibertec_dark.svg` al
lado de `cibertec.svg`, el IDE elige solo cuál usar según el tema del usuario. Si el
logo se lee bien sobre fondo claro y oscuro, basta con el primero.

## Nota

Mientras no haya ningún archivo aquí, el plugin funciona igual: simplemente no muestra
el logo en ningún sitio.
