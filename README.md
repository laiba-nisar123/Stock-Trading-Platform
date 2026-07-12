# 📈 Stock Trading Platform — Pro Edition

A Java Swing desktop simulator where you trade sample stocks with fake
money, styled like a real trading terminal — dark navy background, neon
blue/teal/green/red accents, floating trade & history panels, and a live
portfolio donut chart.

![Java](https://img.shields.io/badge/Java-11%2B-orange?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

---

## 📋 Table of Contents

1. [Overview](#-overview)
2. [Features](#-features)
3. [Screens](#-screens)
4. [Complete Program Flow](#-complete-program-flow)
5. [Project Structure](#-project-structure)
6. [Class-by-Class Reference](#-class-by-class-reference)
7. [Buy / Sell Logic Explained](#-buy--sell-logic-explained)
8. [Data Persistence Format](#-data-persistence-format)
9. [Color Theme](#-color-theme)
10. [Getting Started](#️-getting-started)
11. [Roadmap](#-roadmap)
12. [License](#-license)

---

## 🔎 Overview

The Stock Trading Platform is a desktop simulation built in Java Swing
where a user trades a set of sample stocks using fake money, without
any real financial risk. Every trader starts with a simulated balance
of **$10,000**. They can browse live (randomly fluctuating) stock
prices, buy and sell shares, track their portfolio's profit or loss,
and review a full transaction ledger. All progress is saved to disk
automatically, so closing and reopening the app does not erase
anything.

## ✨ Features

- 🔐 **Login screen** with a procedurally-drawn hero graphic and a dark `#1E1E2E` form
- 📊 **Live-feeling market** — 7 sample stocks, prices fluctuate ±3% on refresh, green ▲ / red ▼ indicators
- 💰 **Realistic Buy/Sell engine** — balance checks, share-ownership checks, and average cost-basis tracking for accurate profit/loss %
- 🍩 **Donut chart** of portfolio allocation (cash vs. each holding)
- 🧾 **Filterable transaction history** (All / Buys / Sells) with a one-click export to a formatted `.txt` statement
- 💾 **Zero-setup persistence** — plain-text file storage via `FileReader`/`FileWriter`, no database required
- 🎨 **Consistent neon dark theme** across every screen, driven by a single `Theme.java` palette

## 🖥 Screens

| Screen | Description |
|---|---|
| **1. Login** | Split layout — hero graphic on the left, dark login form on the right |
| **2. Dashboard** | Balance card, profile card, donut chart, side-nav, and the market/portfolio tables |
| **3. Trade Center** | Floating overlay — BUY/SELL toggle, stock picker, quantity, mock order book |
| **4. History** | Floating overlay — filterable, color-tagged transaction ledger + export |

---

## 🔄 Complete Program Flow

This is the exact path the app takes from launch to close, step by step.

### 1. App Launch
- `LoginScreen.main()` runs. A window opens split into two halves:
  left = generated hero graphic, right = dark `#1E1E2E` login form.

### 2. Login
- User types a **username** (password field exists but any value is
  accepted — this is a risk-free simulation).
- User clicks **"Enter Trading Desk →"**.
- The app checks: *does `data/user_<username>.txt` already exist?*
  - **Yes** → `DataManager.loadUser()` reads the file and restores the
    exact balance, portfolio, cost basis, and full trade history.
  - **No** → a brand-new `User` object is created with the standard
    **$10,000** starting balance.
- `LoginScreen` disposes itself and opens `Dashboard`.

### 3. Dashboard Opens (Screen 2)
- Top row renders: gradient balance card, profile card, donut chart
  (portfolio allocation).
- Center defaults to the **Market Overview** table (all 7 stocks,
  live price, green/red % change column).
- Left side-nav is active: *Market Overview, Trade Center, My
  Portfolio, History, Logout.*

### 4. User Picks a Side-Nav Option
- **Market Overview** → shows the stock table. Clicking **"Refresh
  Prices"** calls `Stock.fluctuatePrice()` on every stock (±3% random
  move) and re-renders the table and donut chart.
- **My Portfolio** → swaps the center panel (via `CardLayout`) to the
  holdings table: Symbol, Shares, Current Value, Profit/Loss %.
- **Trade Center** → opens `TradeCenterOverlay` as a floating card over
  a dimmed glass pane (see step 5).
- **History** → opens `HistoryOverlay` as a floating card (see step 6).
- **Logout** → confirmation dialog, then step 7.

### 5. Trade Center Flow (Screen 3)
1. User toggles **BUY** or **SELL** (green pill / red pill).
2. User picks a stock from the dropdown and types a **quantity**.
3. Price/share and Total update live as they type.
4. User clicks **"⚡ Simulate Transaction"**.
5. **If BUY:**
   - Check: `cost = price × quantity` ≤ current balance?
     - **No** → error dialog ("Insufficient balance"), stays open to retry.
     - **Yes** → `User.buyStock()` runs: balance decreases, portfolio
       quantity increases, cost basis increases by `cost`, a `BUY`
       `Transaction` is logged.
6. **If SELL:**
   - Check: quantity ≤ shares currently owned?
     - **No** → error dialog ("Insufficient shares"), stays open to retry.
     - **Yes** → `User.sellStock()` runs: balance increases by
       proceeds, portfolio quantity decreases, cost basis reduced
       proportionally (using average cost/share), a `SELL`
       `Transaction` is logged.
7. Dashboard refreshes immediately behind the overlay — new balance,
   portfolio table, and donut chart all update.
8. User closes the overlay (✕) to return to the Dashboard.

### 6. History Flow (Screen 4)
1. Overlay opens showing the full transaction ledger, newest first,
   with green **BUY** / red **SELL** tags.
2. User can filter: **All / Buys / Sells** — table re-renders instantly.
3. User can click **"⬇ Export History"**:
   - `DataManager.exportHistoryReport()` writes a formatted `.txt`
     statement to `data/<username>_history_report.txt`.
   - A confirmation dialog shows the saved file path.
4. User closes the overlay (✕) to return to the Dashboard.

### 7. Logout / Exit
- **Logout button** → confirmation dialog → if confirmed,
  `DataManager.saveUser()` writes balance/portfolio/cost-basis/history
  to disk, then the app returns to the **Login screen** (step 2).
- **Closing the window** → same save happens automatically
  (`saveAndExit()`), then the application terminates.

### 8. Next Login
- Because everything was written to `data/user_<username>.txt`, the
  next time that username logs in, `DataManager.loadUser()` restores
  everything exactly as it was left — nothing is lost.

## ScreenShort

<img width="980" height="587" alt="WhatsApp Image 2026-07-12 at 4 54 11 PM" src="https://github.com/user-attachments/assets/3249cfe5-94eb-44f2-8426-19c70c11066c" />
<img width="985" height="572" alt="WhatsApp Image 2026-07-12 at 4 54 12 PM" src="https://github.com/user-attachments/assets/b4dc3921-86e9-482b-932c-a0c13e816771" />
<img width="1129" height="692" alt="WhatsApp Image 2026-07-12 at 4 54 12 PM (1)" src="https://github.com/user-attachments/assets/a220d914-bb79-4838-ae6d-902fd4c4f70d" />
<img width="318" height="198" alt="image" src="https://github.com/user-attachments/assets/e25f0ba2-acc0-4b63-8700-8dbe48705f99" />
<img width="1134" height="677" alt="image" src="https://github.com/user-attachments/assets/169cf56e-6532-420f-80f2-d91d4a3aa2c8" />
<img width="889" height="514" alt="image" src="https://github.com/user-attachments/assets/138ba716-b7fe-40a8-a8c9-3c2a5b562517" />
<img width="1130" height="680" alt="image" src="https://github.com/user-attachments/assets/7214b62b-9646-44e3-bb69-99bd60806fc0" />
<img width="1125" height="651" alt="image" src="https://github.com/user-attachments/assets/c586d199-4016-41f5-bc3a-bdbe0d684fe6" />


---

## 🗂 Project Structure

<img width="789" height="622" alt="image" src="https://github.com/user-attachments/assets/f948037e-0ec3-44c8-ae3c-7d3f72a7aa00" />

## 🧩 Class-by-Class Reference

| File | Role | What it does |
|---|---|---|
| `Theme.java` | Central palette & fonts | Holds every Color/Font constant so all screens stay visually consistent. |
| `Stock.java` | Stock model | symbol, name, price, previousPrice. `fluctuatePrice()` randomly moves price ±3%; `getChangePercent()` drives the ▲/▼ indicator. |
| `User.java` | Trader model | username, balance, portfolio, costBasis, history. `buyStock()`/`sellStock()` update all four together. |
| `Transaction.java` | Trade record | type, symbol, quantity, price, timestamp. `toCsvLine()`/`fromCsvLine()` serialize it without losing the original date/time. |
| `StockMarket.java` | Stock catalogue | Holds the 7 sample stocks and `simulateMarketMovement()` to refresh all prices at once. |
| `DataManager.java` | File persistence | `saveUser()`/`loadUser()` read & write `data/user_<name>.txt`. `exportHistoryReport()` writes a formatted trade statement. |
| `RoundedButton.java` | Reusable button | A `JButton` painted as a rounded pill with base/hover colors and an "active" glow state. |
| `RoundedPanel.java` | Reusable card | A `JPanel` with rounded corners; supports flat fill or a two-color gradient (the balance card) plus an optional glow border. |
| `DonutChartPanel.java` | Portfolio chart | Draws a ring chart from cash + each holding, plus a matching legend. |
| `MarketArtPanel.java` | Login hero graphic | Procedurally paints the login screen's gradient + glowing price-line artwork. |
| `LoginScreen.java` | Screen 1 | Login form; loads or creates the `User`, then opens the Dashboard. |
| `Dashboard.java` | Screen 2 | Main hub — side-nav, balance/profile/donut cards, market/portfolio tables. |
| `TradeCenterOverlay.java` | Screen 3 | Floating BUY/SELL panel with the trade execution logic. |
| `HistoryOverlay.java` | Screen 4 | Floating transaction ledger with filters and export. |

## 💰 Buy / Sell Logic Explained

**Buying:** `cost = price × quantity`. If cost exceeds the balance, the
trade is rejected. Otherwise: balance decreases by cost, portfolio
quantity increases, cost basis increases by cost (so average cost per
share = costBasis ÷ quantity), and a `BUY` transaction is logged.

**Selling:** If requested quantity exceeds shares owned, the trade is
rejected. Otherwise: balance increases by proceeds, portfolio quantity
decreases, and cost basis is reduced *proportionally* using the
average cost per share already held (not just subtracting proceeds) —
this keeps the remaining position's average cost accurate. A `SELL`
transaction is logged.

**Worked example:** Buy 10 shares of AAPL at $180 (cost basis = $1,800).
Price rises to $200, sell 4 shares. Average cost was $180/share, so
$720 of cost basis is removed, leaving 6 shares with a $1,080 cost
basis (still $180/share). Portfolio shows 6 × $200 = $1,200 value —
a profit of $120, i.e. **+11.1%**.

## 💾 Data Persistence Format

Each trader's data lives in `data/user_<username>.txt`:
BALANCE,8420.50
PORTFOLIO,AAPL,30
PORTFOLIO,TSLA,10
COSTBASIS,AAPL,5430.00
COSTBASIS,TSLA,2380.00
HISTORY,BUY,AAPL,30,181.00,12-07-2026 10:14:02
HISTORY,BUY,TSLA,10,238.00,12-07-2026 10:16:47

`DataManager.loadUser()` reads this line by line: `BALANCE` restores
cash, each `PORTFOLIO` line restores a holding's quantity, each
`COSTBASIS` line restores what was actually paid (needed for
profit/loss %), and each `HISTORY` line rebuilds a `Transaction` —
including its original timestamp.

## 🎨 Color Theme

| Purpose | Hex |
|---|---|
| App background | `#0D0F1A` |
| Side-nav / panels | `#141726` |
| Cards / tables | `#1A1D2E` |
| Login panel | `#1E1E2E` |
| Neon Blue (accents/headers) | `#00BFFF` |
| Neon Teal (highlights) | `#00E6D2` |
| Neon Green (Buy / gains) | `#00E676` |
| Neon Red (Sell / losses) | `#FF1744` |
| Neon Purple (secondary cards) | `#B266FF` |

## ⚙️ Getting Started

**Prerequisites:** JDK 11 or newer ([download here](https://adoptium.net/)) — Swing is bundled by default, no extra libraries needed.

```bash
git clone https://github.com/<your-username>/stock-trading-platform.git
cd stock-trading-platform/src
javac *.java
java LoginScreen
```

A window opens directly on the Login screen. Any username works — a
save file is created for it automatically on first login.

**In NetBeans / IntelliJ:** create a new plain Java Application project,
copy the contents of `src/` into its source folder, and set
`LoginScreen` as the main class.

## Flowchart

<img width="1032" height="901" alt="image" src="https://github.com/user-attachments/assets/84aa08cb-1793-45cc-a13c-c80fdbe4c505" />


## 🧭 Roadmap

- [ ] Swap file storage for MySQL/JDBC (only `DataManager.java` needs to change)
- [ ] Real PDF export via iText or Apache PDFBox
- [ ] Continuous price updates via a scheduled `Timer`
- [ ] Per-stock price-history line chart
- [ ] Multi-user login with password hashing

## 📄 License

Released under the MIT License — free to use, modify, and share.
MIT License
Copyright (c) 2026 Stock Trading Platform Contributors
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
