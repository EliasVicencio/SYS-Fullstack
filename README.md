[Caso semestral EFT DSY1106.docx](https://github.com/user-attachments/files/26121381/Caso.semestral.EFT.DSY1106.docx)


```markdown
# Estructura del Proyecto

```
```
ms-pets/
├── src/main/java/com/sanossalvos/mspets/
│   ├── MsPetsApplication.java
│   ├── controller/
│   │   └── PetController.java
│   ├── service/
│   │   ├── PetService.java
│   │   └── PetServiceImpl.java
│   ├── model/
│   │   ├── dto/
│   │   │   ├── PetRequestDTO.java
│   │   │   └── PetResponseDTO.java
│   │   ├── entity/
│   │   │   ├── Pet.java
│   │   │   └── PetType.java (enum: LOST, FOUND)
│   │   └── mapper/
│   │       └── PetMapper.java
│   ├── repository/
│   │   └── PetRepository.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ResourceNotFoundException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/ (si usas Flyway)
└── pom.xml
