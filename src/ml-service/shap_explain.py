import pandas as pd
import numpy as np
import joblib
import shap
import json

print("Carregando modelo e dados...")
modelo = joblib.load("fraud_model.pkl")
dados = pd.read_csv("creditcard.csv")

features = dados.drop(columns=["Class", "Time"])
amostra = features.sample(500, random_state=42)

print("Calculando SHAP values...")
explicador = shap.TreeExplainer(modelo)
shap_values = explicador.shap_values(amostra)

# Diagnóstico — vamos ver o que chegou
print("Tipo do shap_values:", type(shap_values))
print("Shape do shap_values:", np.array(shap_values).shape)

# Se for array 3D (amostras x features x classes), pega a classe fraude
if len(np.array(shap_values).shape) == 3:
    print("Formato 3D detectado — pegando classe fraude (indice 1)")
    shap_fraude = np.array(shap_values)[:, :, 1]
elif isinstance(shap_values, list):
    print("Formato lista detectado — pegando indice 1")
    shap_fraude = np.array(shap_values[1])
else:
    print("Formato simples detectado")
    shap_fraude = np.array(shap_values)

print("Shape final do shap_fraude:", shap_fraude.shape)

# Calcula importância média por feature
importancia_media = np.abs(shap_fraude).mean(axis=0)
print("Tamanho importancia_media:", len(importancia_media))
print("Numero de features:", len(features.columns))

# Monta dicionário
importancia_por_feature = {}
nomes_features = features.columns.tolist()

for i in range(len(nomes_features)):
    nome = nomes_features[i]
    valor = float(importancia_media[i])
    importancia_por_feature[nome] = valor

# Ordena
importancia_ordenada = dict(
    sorted(importancia_por_feature.items(), key=lambda item: item[1], reverse=True)
)

print("\n=== TOP 10 FEATURES MAIS IMPORTANTES PARA DETECTAR FRAUDE ===")
posicao = 1
for nome_feature, valor_importancia in list(importancia_ordenada.items())[:10]:
    print(str(posicao) + ". " + nome_feature + ": " + str(round(valor_importancia, 4)))
    posicao = posicao + 1

with open("shap_importance.json", "w") as arquivo:
    json.dump(importancia_ordenada, arquivo, indent=2)

print("\nArquivo shap_importance.json criado.")
print("Concluido!")