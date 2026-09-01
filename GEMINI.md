# Workspace Guidelines

Always read and strictly adhere to the vertical slicing and horizontal layering patterns defined in [ARCHITECTURAL_GUIDELINES.md](./ARCHITECTURAL_GUIDELINES.md) when creating, modifying, or refactoring any package or class in this project.

## Core Rules:
1. **Vertical Component Slicing**: Ensure that components are vertically sliced (e.g., `gr.bytethat.gsis.registry`, `gr.bytethat.gsis.gsis39a`) with common dependencies in `gr.bytethat.gsis.common`.
2. **Horizontal Layering**: Maintain three strict packages within each vertical module:
   - `abstractions`: Pure interfaces, domain models, and custom exceptions. Absolutely **no framework dependencies** (no Spring annotations, no Jackson annotations).
   - `core`: Pure Java business logic implementing the abstractions. Framework-agnostic.
   - `infrastructure`: Low-level integrations (Spring beans, `@ConfigurationProperties`, JAX-WS handlers, Jackson bindings, REST controllers).
3. **No Annotation Leakage**: Do not add third-party or framework annotations directly inside abstractions/core models. All serialization bindings must reside in the component's `infrastructure` layer.
