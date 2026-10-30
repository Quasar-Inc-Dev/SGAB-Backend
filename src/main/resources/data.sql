INSERT INTO livro (pha, dewey, isbn, titulo, subtitulo, descricao, autor, editora, livro_status, idioma, area, paginas, ano, genero, tags)
VALUES
('B001', '005.1', '9780132350884', 'Clean Code', 'A Handbook of Agile Software Craftsmanship',
 'Boas práticas para escrever código limpo e de fácil manutenção.',
 'Robert C. Martin', 'Prentice Hall', 'ATIVO', 'Inglês', 'Tecnologia', 464, 2008, 'Técnico', 'programação,boas práticas,engenharia de software'),

('B002', '005.1', '9788576082675', 'Código Limpo', 'Habilidades Práticas do Agile Software',
 'Guia prático para desenvolvimento de software com qualidade.',
 'Robert C. Martin', 'Alta Books', 'ATIVO', 'Português', 'Tecnologia', 431, 2009, 'Técnico', 'programação,agile,código'),
 
('B003', '813', '9788582850350', 'Dom Casmurro', NULL,
 'Clássico da literatura brasileira sobre ciúme e dúvida.',
 'Machado de Assis', 'Penguin-Companhia', 'ATIVO', 'Português', 'Literatura', 256, 1899, 'Romance', 'clássico,literatura brasileira,romance'),
 
('B004', '813', '9788582850343', 'O Cortiço', NULL,
 'Romance naturalista sobre a vida em um cortiço no Rio de Janeiro.',
 'Aluísio Azevedo', 'Penguin-Companhia', 'ATIVO', 'Português', 'Literatura', 304, 1890, 'Romance', 'naturalismo,clássico,literatura brasileira'),
 
('B005', '005.133', '9788543004792', 'Java: Como Programar', NULL,
 'Introdução completa à linguagem Java para iniciantes.',
 'Paul Deitel', 'Pearson', 'ATIVO', 'Português', 'Tecnologia', 1128, 2017, 'Técnico', 'java,programação,orientação a objetos'),
 
('B006', '005.756', '9788575227435', 'Aprendendo SQL', NULL,
 'Introdução prática à linguagem SQL e aos bancos de dados relacionais.',
 'Alan Beaulieu', 'Novatec', 'ATIVO', 'Português', 'Tecnologia', 512, 2019, 'Técnico', 'sql,banco de dados,consultas'),
 
('B007', '158.1', '9788539004119', 'O Poder do Hábito', 'Por que fazemos o que fazemos na vida e nos negócios',
 'Como os hábitos são formados e podem ser transformados.',
 'Charles Duhigg', 'Objetiva', 'ATIVO', 'Português', 'Psicologia', 408, 2012, 'Não Ficção', 'hábitos,comportamento,psicologia'),
 
('B008', '153.8', '9788539003839', 'Rápido e Devagar', 'Duas Formas de Pensar',
 'Estudo sobre os dois sistemas de pensamento humano.',
 'Daniel Kahneman', 'Objetiva', 'ATIVO', 'Português', 'Psicologia', 624, 2011, 'Não Ficção', 'psicologia,economia comportamental,tomada de decisão'),
 
('B009', '863', '9788501012074', 'Cem Anos de Solidão', NULL,
 'Saga da família Buendía na cidade fictícia de Macondo.',
 'Gabriel García Márquez', 'Record', 'ATIVO', 'Português', 'Literatura', 448, 1967, 'Realismo Mágico', 'clássico,literatura latino-americana,ficção'),
 
('B010', '005.7', '9788535236996', 'Algoritmos', 'Teoria e Prática',
 'Conceitos fundamentais de algoritmos e estrutura de dados.',
 'Thomas H. Cormen', 'Elsevier', 'ATIVO', 'Português', 'Tecnologia', 936, 2012, 'Técnico', 'algoritmos,estrutura de dados,computação'),
 
('B011', '005.117', '9788550804606', 'Arquitetura Limpa', 'O Guia do Artesão para Estrutura e Design de Software',
 'Princípios de arquitetura de software sustentáveis e independentes de framework.',
 'Robert C. Martin', 'Alta Books', 'ATIVO', 'Português', 'Tecnologia', 432, 2019, 'Técnico', 'arquitetura,design de software,engenharia'),
 
('B012', '791.43', '9788585910174', 'A Sociedade do Espetáculo', NULL,
 'Crítica à sociedade de consumo e à cultura da imagem.',
 'Guy Debord', 'Contraponto', 'ATIVO', 'Português', 'Filosofia', 240, 1967, 'Ensaio', 'filosofia,crítica social,sociedade'),
 
('B013', '005.2', '9788573076103', 'Padrões de Projeto', 'Soluções Reutilizáveis de Software Orientado a Objetos',
 'Catálogo clássico de padrões de projeto para desenvolvimento de software.',
 'Erich Gamma', 'Bookman', 'ATIVO', 'Português', 'Tecnologia', 395, 2000, 'Técnico', 'design patterns,orientação a objetos,engenharia de software'),
 
('B014', '158', '9788547000240', 'Mindset', 'A Nova Psicologia do Sucesso',
 'Como a mentalidade de crescimento pode transformar resultados.',
 'Carol S. Dweck', 'Objetiva', 'ATIVO', 'Português', 'Psicologia', 328, 2006, 'Não Ficção', 'mentalidade,desenvolvimento pessoal,psicologia'),
 
('B015', '005.4', '9788543005676', 'Sistemas Operacionais Modernos', NULL,
 'Fundamentos e conceitos avançados de sistemas operacionais.',
 'Andrew S. Tanenbaum', 'Pearson', 'ATIVO', 'Português', 'Tecnologia', 800, 2016, 'Técnico', 'sistemas operacionais,computação,hardware');

-- Exemplares (cópias físicas): 1 por livro, e uma 2ª cópia para os 5 primeiros
INSERT INTO exemplar (livro_id, tombo, status, data_aquisicao, versao)
SELECT livro_id, CONCAT('L', livro_id, '-001'), 'DISPONIVEL', CURRENT_TIMESTAMP, 0 FROM livro;

INSERT INTO exemplar (livro_id, tombo, status, data_aquisicao, versao)
SELECT livro_id, CONCAT('L', livro_id, '-002'), 'DISPONIVEL', CURRENT_TIMESTAMP, 0 FROM livro WHERE livro_id <= 5;
