1. **Create an Input File**:
   - Create a file named `input.txt` or whatever you would like to name it in the same directory where the JAR file (`MyProgram.jar`) is located. This file will be used as input for the program.

2. **Install Java**:
   - Ensure that Java is installed on your machine. If you don't have Java installed, you can download it from [this link](https://www.java.com/en/download/).

3. **Run the JAR File**:
   - Open a terminal, command prompt, PowerShell, or the integrated terminal in Visual Studio Code.
   - Navigate to the directory where the JAR file (`MyProgram.jar`) is located.
   - Execute the following command to run the JAR file:

   ```bash
   java -jar MyProgram.jar
   ```
4. **Input**:
   - The input file should contain the input data for the program - ensure that tokens are delimited with spaces and all function parameters are pre declared in a parent or global scope. Here is an example of the input format:

   ```
   main
   num V_a, num V_b, num V_c,
   begin
      V_a = F_euclid(9, 3, 0);
      print V_a;
   end

   num F_euclid(V_a, V_b, V_c)
   {
      num V_d, text V_e, text V_f,
      begin

         if eq(V_a, V_b)
         then
               begin
                  return V_a;
               end
         else
               begin
                  if grt(V_a, V_b)
                  then
                     begin
                           V_a = sub(V_a, V_b);
                     end
                  else
                     begin
                           V_b = sub(V_b, V_a);
                     end;
                  V_c = F_euclid(V_a, V_b, V_c);
                  return V_c;
               end;
      end
   }
   end
   ```
6. **Run the Program**:
   - While running the program, you will be prompted to enter the name of the input file. Enter the name of the input file (e.g., `input.txt`) and press Enter.
5. **View the Output**:
   - The program will read the input from the `input file`, process it, and display the output in the terminal as well as write to an output folder - outputs included
      - Syntax tree before scope analyzing.
      - Syntax tree after scope analyzing.
      - Intermediate code - For phase 5.a and 5.b.
      - Target code - For phase 5.b
   The rest is outputed to the terminal.

6. **Run Output BASIC**:
   - To run the output BASIC code, install the BASIC interpreter on your machine and paste in the target code from the output folder. You can download a BASIC interpreter from [this link](https://qb64.com).


