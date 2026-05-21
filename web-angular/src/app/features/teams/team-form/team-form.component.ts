import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Team, TeamUpsertRequest } from '../../../core/models/team.model';

@Component({
  selector: 'app-team-form',
  imports: [ReactiveFormsModule],
  templateUrl: './team-form.component.html',
  styleUrl: './team-form.component.scss',
})
export class TeamFormComponent {
  private readonly formBuilder = inject(FormBuilder);

  readonly title = input.required<string>();
  readonly descriptionText = input<string>('');
  readonly submitLabel = input<string>('Save team');
  readonly initialValue = input<Team | null>(null);
  readonly isSubmitting = input<boolean>(false);
  readonly errorMessage = input<string>('');
  readonly submitted = output<TeamUpsertRequest>();

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(4000)],
    active: [true],
  });

  constructor() {
    effect(() => {
      const team = this.initialValue();
      if (team) {
        this.form.reset({
          name: team.name,
          description: team.description ?? '',
          active: team.active,
        });
      }
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { name, description, active } = this.form.getRawValue();
    this.submitted.emit({
      name,
      description: description.trim() ? description.trim() : null,
      active,
    });
  }
}
