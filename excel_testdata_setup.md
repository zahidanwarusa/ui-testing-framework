co# TestData.xlsx Setup

Create a file called `TestData.xlsx` in `src/main/resources/excel/` with the following structure:

## Sheet Name: TestData

| TestID | URL | Username | Password | Remarks | ExpectedResult |
|--------|-----|----------|----------|---------|----------------|
| CBP001 | https://tf-sat.cbp.dhs.gov/pax/LoginPage | | | CBP Login Test | Login Successful |
| CBP002 | https://tf-sat.cbp.dhs.gov/uv/hotlists/ntc/traveler | | | Automated 1-Day Lookout Creation | Form Filled |

**Column Descriptions:**
- **TestID**: Must match the TestID from TestRunner.xlsx
- **URL**: Base URL for the test (if needed)
- **Username/Password**: Login credentials (if needed - usually handled by Windows auth)
- **Remarks**: Default remarks for form filling
- **ExpectedResult**: What you expect to happen

**Note**: For CBP tests, username/password are usually not needed since Windows authentication is used.