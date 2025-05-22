-- Adiciona a coluna dataMovimento na tabela Movimento
ALTER TABLE Movimento ADD dataMovimento DATETIME NOT NULL DEFAULT GETDATE(); 