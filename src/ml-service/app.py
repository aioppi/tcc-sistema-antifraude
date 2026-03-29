from flask import Flask, request, jsonify
import joblib
import numpy as np
import json



app = Flask(__name__)

print("Carregando modelo...")
model = joblib.load("fraud_model.pkl")
print("Modelo carregado.")

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "online", "model": "RandomForest"})

@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.get_json()
        features = data["features"]  # lista de 29 valores (V1-V28 + Amount)
        input_array = np.array(features).reshape(1, -1)
        probability = model.predict_proba(input_array)[0][1]
        prediction = int(probability >= 0.5)
        return jsonify({
            "fraud_probability": round(float(probability), 4),
            "is_fraud": bool(prediction),
            "model": "RandomForest"
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 400

# Carrega importâncias SHAP calculadas previamente
import os
shap_data = {}
if os.path.exists("shap_importance.json"):
    with open("shap_importance.json", "r") as f:
        shap_data = json.load(f)
    print("SHAP importance carregado.")

@app.route("/explain", methods=["GET"])
def explain():
    # Retorna as 10 features mais importantes segundo SHAP
    top10 = list(shap_data.items())[:10]
    resultado = []
    for nome, valor in top10:
        resultado.append({
            "feature": nome,
            "importancia": round(valor, 4)
        })
    return jsonify({
        "modelo": "RandomForest",
        "metodo_explicabilidade": "SHAP TreeExplainer",
        "top10_features": resultado
    })



if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=False)