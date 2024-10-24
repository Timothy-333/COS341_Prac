1. **Create an Input File**:
   - Create a file named `input.txt` in the same directory where the JAR file (`MyProgram.jar`) is located. This file will be used as input for the program.

2. **Install Java**:
   - Ensure that Java is installed on your machine. If you don't have Java installed, you can download it from [this link](https://www.java.com/en/download/).

3. **Run the JAR File**:
   - Open a terminal, command prompt, PowerShell, or the integrated terminal in Visual Studio Code.
   - Navigate to the directory where the JAR file (`MyProgram.jar`) is located.
   - Execute the following command to run the JAR file:

   ```bash
   java -jar MyProgram.jar
   ```
4. **Example Input**:
   - The `input.txt` file should contain the input data for the program. Here is an example of the input format:

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
5. **View the Output**:
   - The program will read the input from the `input.txt` file, process it, and display the output in the terminal as well as write to an output folder.

6. **Run Output BASIC**:
   - To run the output BASIC code, install the BASIC interpreter on your machine. You can download a BASIC interpreter from [this link](https://qb64.com).


