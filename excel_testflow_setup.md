# TestFlow.xlsx Setup

Create a file called `TestFlow.xlsx` in `src/main/resources/excel/` with the following structure:

## Sheet Name: TestFlow

| TestID | Keyword1 | Keyword2 | Keyword3 | Keyword4 | Keyword5 |
|--------|----------|----------|----------|----------|----------|
| CBP001 | OPEN_BROWSER | CBP_LOGIN | CLOSE_BROWSER | | |
| CBP002 | OPEN_BROWSER | CBP_LOGIN | CREATE_AND_FILL_1DAY_LOOKOUT | CLOSE_BROWSER | |

**Column Descriptions:**
- **TestID**: Must match the TestID from TestRunner.xlsx
- **Keyword1-20**: Keywords to execute in sequence (you can add more columns up to Keyword20)

**Available Keywords:**
- `OPEN_BROWSER` - Opens the browser
- `CBP_LOGIN` - Logs into CBP system
- `CREATE_AND_FILL_1DAY_LOOKOUT` - Complete 1-day lookout workflow
- `CLOSE_BROWSER` - Closes the browser