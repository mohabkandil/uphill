dev:
	docker compose up -d postgres redis mock-external
	@echo "Starting local development environment..."
	@echo "PostgreSQL: localhost:5432"
	@echo "Redis: localhost:6379"
	@echo "Mock External: localhost:3001"
	@echo "Run: ./mvnw spring-boot:run -Dspring.profiles.active=local"

run:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

rebuild:
	docker compose down --volumes --remove-orphans
	docker compose up --build

ps:
	docker compose ps

unit-test:
	@echo "Running unit tests..."
	./mvnw test

docs-diagrams:
	@echo "Rendering Mermaid diagrams to SVG..."
	docker run --rm -u $$(id -u):$$(id -g) -v $$(pwd):/data minlag/mermaid-cli:10.9.0 \
	  -i docs/diagrams/architecture.mmd -o docs/diagrams/architecture.svg
	docker run --rm -u $$(id -u):$$(id -g) -v $$(pwd):/data minlag/mermaid-cli:10.9.0 \
	  -i docs/diagrams/class.mmd -o docs/diagrams/class.svg
	docker run --rm -u $$(id -u):$$(id -g) -v $$(pwd):/data minlag/mermaid-cli:10.9.0 \
	  -i docs/diagrams/seq-create-appointment.mmd -o docs/diagrams/seq-create-appointment.svg
	docker run --rm -u $$(id -u):$$(id -g) -v $$(pwd):/data minlag/mermaid-cli:10.9.0 \
	  -i docs/diagrams/seq-outbox.mmd -o docs/diagrams/seq-outbox.svg
	docker run --rm -u $$(id -u):$$(id -g) -v $$(pwd):/data minlag/mermaid-cli:10.9.0 \
	  -i docs/diagrams/flow-booking.mmd -o docs/diagrams/flow-booking.svg
	docker run --rm -u $$(id -u):$$(id -g) -v $$(pwd):/data minlag/mermaid-cli:10.9.0 \
	  -i docs/diagrams/state-appointment.mmd -o docs/diagrams/state-appointment.svg
	@echo "Diagrams rendered under docs/diagrams/*.svg"
