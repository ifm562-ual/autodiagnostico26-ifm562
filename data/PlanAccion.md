# 🚗 MAPEO COMPLETO DE MARCAS → GRUPOS AUTOMOTRICES

Este documento es para uso de otra IA que procesará un JSON lleno de vehículos de modelos diversos. Aquí tienes un **mapeo limpio y exhaustivo de tu lista exacta de marcas → grupos automotrices**, optimizado para inferir componentes compartidos. Incluye **propiedad y consideraciones clave (históricas o técnicas)** cuando afectan materialmente la compatibilidad de piezas/plataformas.

---

## 🇩🇪 Grupo Volkswagen (VAG)

**Marcas:**

* Audi
* Bentley
* Bugatti *(→ ahora Bugatti-Rimac, pero históricamente VAG)*
* Cupra
* Lamborghini
* Porsche
* SEAT
* Škoda
* Volkswagen

**Realidad de ingeniería:**

* Ecosistema más fuerte para reutilización de piezas
* Plataformas: MQB, MLB, MEB
* Motores: serie EA (p. ej., 1.9 TDI, 2.0 TSI)

**Heurística:** ALTA confianza

---

## 🇳🇱 Stellantis (PSA + FCA)

**Marcas:**

* Abarth
* Alfa Romeo
* Chrysler
* Citroën
* Dodge
* DS
* Fiat
* Jeep
* Lancia
* Maserati
* Opel
* Peugeot
* Vauxhall

**Realidad de ingeniería:**

* Compartición moderna: CMP, EMP2, STLA
* Modelos antiguos (pre-2021): arquitecturas PSA vs FCA separadas

**Heurística:** ALTA (post-2015), MEDIA (anteriores)

---

## 🇯🇵 Grupo Toyota

**Marcas:**

* Toyota
* Lexus
* Daihatsu

**Realidad de ingeniería:**

* Estandarización plataforma TNGA
* Daihatsu = brazo de ingeniería de autos pequeños

**Heurística:** ALTA

---

## 🇰🇷 Hyundai Motor Group

**Marcas:**

* Hyundai
* Kia
* Genesis

**Heurística:** ALTA (plataformas y motores compartidos)

---

## 🇩🇪 BMW Group

**Marcas:**

* BMW
* Mini
* Rolls-Royce

**Heurística:** ALTA (BMW ↔ Mini), MEDIA (Rolls-Royce más aislado)

---

## 🇨🇳 Grupo Geely

**Marcas:**

* Volvo
* Polestar
* Lotus

**Realidad de ingeniería:**

* Plataformas compartidas: SPA, CMA, SEA
* Volvo ↔ Polestar = muy estrecho

**Heurística:** ALTA (modelos modernos)

---

## 🇺🇸 General Motors (GM)

**Marcas:**

* Buick
* Cadillac
* Chevrolet

**Heurística:** ALTA (especialmente motores y plataformas)

---

## 🇺🇸 Ford Motor Company

**Marcas:**

* Ford
* Lincoln

**Heurística:** ALTA

---

## 🇯🇵 Alianza Renault–Nissan–Mitsubishi

**Marcas:**

* Renault
* Nissan
* Mitsubishi

**Realidad de ingeniería:**

* Existen plataformas compartidas, pero menos uniformes que VAG

**Heurística:** MEDIA

---

## 🇩🇪 Mercedes-Benz Group

**Marcas:**

* Mercedes-Benz
* Smart *(joint venture con Geely)*

**Heurística:**

* Mercedes: independiente
* Smart: mixta (modelos nuevos basados en Geely)

---

## 🇨🇳 SAIC Motor

**Marcas:**

* MG

---

## 🇨🇳 BYD

**Marcas:**

* BYD

---

## 🇺🇸 Tesla

**Marcas:**

* Tesla

---

## 🇮🇹 Ferrari (independiente)

* Sin compartición real de piezas fuera de historia limitada con Maserati

---

## 🇭🇷 Bugatti-Rimac

* Bugatti ahora aquí (ya no puro VAG)

---

## 🇬🇧 Tata Motors

**Marcas:**

* Jaguar
* Land Rover

---

## 🇯🇵 Honda

* Honda

---

## 🇯🇵 Mazda

* Mayormente independiente
* Alguna colaboración reciente con Toyota

---

## 🇯🇵 Subaru

* Independiente
* ~20% propiedad de Toyota

---

## 🇯🇵 Suzuki

* Independiente
* Fuertes lazos recientes con Toyota

---

## 🇬🇧 Aston Martin

* Independiente
* Usa motores/electrónica Mercedes

---

## 🇬🇧 McLaren

* Totalmente independiente

---

## 🇻🇳 VinFast

* Independiente
* Usa tecnología BMW licenciada (modelos iniciales)

---

## 🇫🇷 Alpine

* Parte del Grupo Renault

---

## 🇷🇴 Dacia

* Propiedad de Renault
* Gran compartición con Renault

---

## 🇯🇵 Infiniti

* Nissan (Alianza)

---

## 🇯🇵 Acura

* Honda

---

## 🇪🇺 Rover (defunta / histórica)

* Sin grupo actual
* Históricas: BMW, luego MG Rover

---

## 🇸🇪 Saab (defunta)

* Marca antigua de GM

---

## 🇨🇳 Aiways

* Independiente

---

# ⚙️ Lo que Realmente Importa para tu App

## 1. Jerarquía de señales (práctico)

Usa este orden:

1. **Plataforma (LO MÁS IMPORTANTE)**
2. **Código/familia de motor**
3. **Propiedad del grupo**
4. **Rango de años**

---

## 2. Coincidencias cross-brand de alta confianza

“Minería de oro”:

* **Cluster VAG:** VW ↔ Audi ↔ SEAT ↔ Škoda ↔ Cupra
* **Cluster Stellantis (moderno):** Peugeot ↔ Opel ↔ Citroën ↔ Fiat
* **Cluster Hyundai:** Hyundai ↔ Kia ↔ Genesis
* **Cluster Toyota:** Toyota ↔ Lexus ↔ Daihatsu
* **Cluster Renault:** Renault ↔ Dacia ↔ Nissan *(parcial)*

---

## 3. Suposiciones peligrosas (evitar)

* Mismo grupo ≠ mismas piezas

  * Ejemplo: Jeep ≠ Peugeot (incluso en Stellantis)
* Marcas de lujo suelen divergir

  * Audi vs Lamborghini → overlap muy limitado
* El tiempo importa mucho

  * Opel (pre-2017 GM) ≠ Opel (post-2017 Stellantis)
