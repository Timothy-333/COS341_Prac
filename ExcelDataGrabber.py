import pandas as pd

# Read the Excel file
df = pd.read_excel('SLRParseTable.xlsx')

# Extract column headers
headers = df.columns.tolist()

# Initialize the Java code
java_code = """
public class SLRParseTable {
    public static final String[] HEADERS = {
\t\t"""

# Add headers to the Java code
for header in headers[1:]:
    java_code += f'"{header}", '
java_code = java_code.rstrip(', ')  # Remove trailing comma and space
java_code += """
    };

    public static final String[][] PARSE_TABLE = {
"""

# Iterate through the DataFrame and generate Java code
for index, row in df.iterrows():
    java_code += "\t\t{"
    for item in row[1:]:
        if pd.isna(item) or item == '':
            java_code += 'null, '
        else:
            java_code += f'"{item}", '
    java_code = java_code.rstrip(', ')  # Remove trailing comma and space
    java_code += "},\n"

# Close the Java array and class
java_code += """
    };
}
"""

# Write the Java code to the file
with open('SLRParseTable.java', 'w') as file:
    file.write(java_code)

print("SLR parse table has been successfully converted to Java code.")