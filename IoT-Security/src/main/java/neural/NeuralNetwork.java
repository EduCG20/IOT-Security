package neural;


import java.util.Random;

public class NeuralNetwork {
    private int inputSize;
    private int hiddenSize;
    private int outputSize;
    
    private double[][] weightsInputHidden;
    private double[][] weightsHiddenOutput;
    private double[] biasHidden;
    private double[] biasOutput;
    
    private double learningRate;
    private Random random;
    
    // Capas para forward propagation
    private double[] hiddenOutput;
    private double[] finalOutput;
    
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize, double learningRate) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.learningRate = learningRate;
        this.random = new Random(42);
        
        // Inicializar matrices de pesos
        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        biasHidden = new double[hiddenSize];
        biasOutput = new double[outputSize];
        
        // Inicialización Xavier/Glorot
        double inputLimit = Math.sqrt(6.0 / (inputSize + hiddenSize));
        double hiddenLimit = Math.sqrt(6.0 / (hiddenSize + outputSize));
        
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] = random.nextDouble() * 2 * inputLimit - inputLimit;
            }
        }
        
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weightsHiddenOutput[i][j] = random.nextDouble() * 2 * hiddenLimit - hiddenLimit;
            }
            biasHidden[i] = random.nextDouble() * 2 - 1;
        }
        
        for (int j = 0; j < outputSize; j++) {
            biasOutput[j] = random.nextDouble() * 2 - 1;
        }
    }
    
    // ReLU
    private double relu(double x) {
        return Math.max(0, x);
    }
    
    // Derivada de ReLU
    private double reluDerivative(double x) {
        return x > 0 ? 1 : 0;
    }
    
    // Sigmoid
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    // Derivada de Sigmoid
    private double sigmoidDerivative(double x) {
        double s = sigmoid(x);
        return s * (1 - s);
    }
    
    // Forward propagation
    public double[] forward(double[] inputs) {
        // Capa oculta
        hiddenOutput = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            double sum = biasHidden[i];
            for (int j = 0; j < inputSize; j++) {
                sum += inputs[j] * weightsInputHidden[j][i];
            }
            hiddenOutput[i] = relu(sum);
        }
        
        // Capa de salida
        finalOutput = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            double sum = biasOutput[i];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hiddenOutput[j] * weightsHiddenOutput[j][i];
            }
            finalOutput[i] = sigmoid(sum);
        }
        
        return finalOutput;
    }
    
    // Backpropagation
    public void backward(double[] inputs, double[] targets) {
        // Calcular errores de salida
        double[] outputErrors = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            outputErrors[i] = (targets[i] - finalOutput[i]) * sigmoidDerivative(finalOutput[i]);
        }
        
        // Calcular errores de capa oculta
        double[] hiddenErrors = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            double error = 0;
            for (int j = 0; j < outputSize; j++) {
                error += outputErrors[j] * weightsHiddenOutput[i][j];
            }
            hiddenErrors[i] = error * reluDerivative(hiddenOutput[i]);
        }
        
        // Actualizar pesos oculta -> salida
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weightsHiddenOutput[i][j] += learningRate * outputErrors[j] * hiddenOutput[i];
            }
        }
        
        // Actualizar biases de salida
        for (int i = 0; i < outputSize; i++) {
            biasOutput[i] += learningRate * outputErrors[i];
        }
        
        // Actualizar pesos entrada -> oculta
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] += learningRate * hiddenErrors[j] * inputs[i];
            }
        }
        
        // Actualizar biases de oculta
        for (int i = 0; i < hiddenSize; i++) {
            biasHidden[i] += learningRate * hiddenErrors[i];
        }
    }
    
    // Entrenar
    public void train(double[][] inputs, double[][] targets, int epochs) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalLoss = 0;
            for (int i = 0; i < inputs.length; i++) {
                forward(inputs[i]);
                backward(inputs[i], targets[i]);
                
                for (int j = 0; j < outputSize; j++) {
                    double diff = targets[i][j] - finalOutput[j];
                    totalLoss += diff * diff;
                }
            }
            
            if (epoch % 500 == 0) {
                System.out.println("Época " + epoch + " - Pérdida: " + (totalLoss / inputs.length));
            }
        }
    }
    
    // Predecir
    public double predict(double[] inputs) {
        double[] output = forward(inputs);
        return output[0];
    }
}