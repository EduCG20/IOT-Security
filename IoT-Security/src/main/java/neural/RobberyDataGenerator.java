package neural;

public class RobberyDataGenerator {

    public static double[][] getTrainingInputs() {
        // [horaNormalizada, díaSemanaNormalizado, esFinDeSemana, esFestivo]
        return new double[][] {
            // MADRUGADAS (alto riesgo)
            {2.0/24.0, 0.0/6.0, 0.0, 0.0},
            {3.0/24.0, 0.0/6.0, 0.0, 0.0},
            {4.0/24.0, 0.0/6.0, 0.0, 0.0},
            
            // MADRUGADAS FINDE (riesgo muy alto)
            {2.0/24.0, 5.0/6.0, 1.0, 0.0},
            {3.0/24.0, 6.0/6.0, 1.0, 0.0},
            {4.0/24.0, 6.0/6.0, 1.0, 0.0},
            
            // DÍA LABORABLE (riesgo bajo)
            {10.0/24.0, 0.0/6.0, 0.0, 0.0},
            {11.0/24.0, 0.0/6.0, 0.0, 0.0},
            {14.0/24.0, 0.0/6.0, 0.0, 0.0},
            {15.0/24.0, 0.0/6.0, 0.0, 0.0},
            {16.0/24.0, 0.0/6.0, 0.0, 0.0},
            
            // DÍA FINDE (riesgo bajo-medio)
            {14.0/24.0, 5.0/6.0, 1.0, 0.0},
            {15.0/24.0, 5.0/6.0, 1.0, 0.0},
            
            // NOCHE LABORABLE (riesgo medio)
            {20.0/24.0, 0.0/6.0, 0.0, 0.0},
            {21.0/24.0, 0.0/6.0, 0.0, 0.0},
            {22.0/24.0, 0.0/6.0, 0.0, 0.0},
            {23.0/24.0, 0.0/6.0, 0.0, 0.0},
            
            // NOCHE FINDE (riesgo medio-alto)
            {22.0/24.0, 5.0/6.0, 1.0, 0.0},
            {23.0/24.0, 6.0/6.0, 1.0, 0.0},
            
            // FESTIVOS (riesgo alto incluso de día)
            {14.0/24.0, 0.0/6.0, 0.0, 1.0},
            {15.0/24.0, 0.0/6.0, 0.0, 1.0},
            {16.0/24.0, 0.0/6.0, 0.0, 1.0},
            
            // MAÑANA TEMPRANO (riesgo bajo)
            {6.0/24.0, 0.0/6.0, 0.0, 0.0},
            {7.0/24.0, 0.0/6.0, 0.0, 0.0},
            {8.0/24.0, 0.0/6.0, 0.0, 0.0},
            {9.0/24.0, 0.0/6.0, 0.0, 0.0}
        };
    }

    public static double[][] getTrainingOutputs() {
        // Probabilidad de robo (0 a 1)
        return new double[][] {
            // MADRUGADAS
            {0.85}, {0.88}, {0.80},
            
            // MADRUGADAS FINDE
            {0.95}, {0.96}, {0.94},
            
            // DÍA LABORABLE
            {0.10}, {0.12}, {0.15}, {0.13}, {0.11},
            
            // DÍA FINDE
            {0.25}, {0.28},
            
            // NOCHE LABORABLE
            {0.55}, {0.58}, {0.62}, {0.60},
            
            // NOCHE FINDE
            {0.72}, {0.75},
            
            // FESTIVOS
            {0.82}, {0.85}, {0.80},
            
            // MAÑANA TEMPRANO
            {0.05}, {0.03}, {0.04}, {0.06}
        };
    }
}